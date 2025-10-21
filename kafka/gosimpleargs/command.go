/**
 * Copyright 2016 Confluent Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Example function-based Apache Kafka producer
package main

import (
	"context"
	"fmt"
	"strconv"

	"github.com/confluentinc/confluent-kafka-go/v2/kafka"
	"github.com/confluentinc/confluent-kafka-go/v2/schemaregistry"
	"github.com/magiconair/properties"
	"github.com/urfave/cli/v3"
)

type SubCommand string

type ClientType string

const (
	Produce SubCommand = "produce"
	Consume SubCommand = "consume"

	Producer ClientType = "producer"
	Consumer ClientType = "consumer"
)

type KafkaCommand struct {
	SubCommand
	SchemaType
	BootstrapServer string
	ConfigFile      string
	SRConfigFile    string
	SchemaFile      string
	SchemaId        int64
	Topic           string
	Transactional   bool

	SampleData  string
	RecordCount int64
	TPS         int64
	Earliest    bool
	Latest      bool

	Properties   *properties.Properties
	SRProperties *properties.Properties
}

func (k *KafkaCommand) getSchemaInfo(src schemaregistry.Client) (*schemaregistry.SchemaInfo, error) {
	if k.SchemaId > 0 {
		if subVersions, err := src.GetSubjectsAndVersionsByID(int(k.SchemaId)); err != nil {
			return nil, err
		} else {
			if len(subVersions) != 1 {
				return nil, fmt.Errorf("expected 1 element for schemaID %d but got %d", k.SchemaId, len(subVersions))
			}
			for _, subVersion := range subVersions {
				schema, err := src.GetBySubjectAndID(subVersion.Subject, int(k.SchemaId))
				return &schema, err
			}
		}
	} else if k.SchemaFile != "" {
		return k.SchemaType.loadSchema(k.SchemaFile)
	} else { // TODO Assume topic value schema is requested
	}
	return nil, fmt.Errorf("invalid condition to get schemaInfo")
}

func NewKafkaCommand() *KafkaCommand {
	return &KafkaCommand{}
}

func (k *KafkaCommand) getConsumer() (*kafka.Consumer, error) {
	configMap, err := k.getClientConfig()
	if err != nil {
		return nil, err
	}

	c, err := kafka.NewConsumer(configMap)
	if err != nil {
		errorLogger.Fatalf("Failed to create consumer: %s\n", err)
		return nil, err
	}
	return c, nil
}

func (k *KafkaCommand) getValueObject() (interface{}, error) {
	for _, sampleData := range sampleDataList {
		if sampleData.SchemaType == k.SchemaType && sampleData.SampleDatatype == k.SampleData {
			return sampleData.NewFunc(), nil
		}
	}
	return nil, fmt.Errorf("unsupported sample format %s and data object %s", k.SchemaType, k.SampleData)
}

// func (k *KafkaCommand) getDeserializer() (*schemaregistry.Client, *serde.Deserializer, error) {
// 	if !k.SchemaType.needsDataContract() {
// 		return nil, nil, fmt.Errorf("schemaregistry not needed for %s format", k.SchemaType)
// 	}
// 	srConfig, err := k.getSRClientConfig()
// 	if err != nil {
// 		return nil, nil, err
// 	}
// 	src, err := schemaregistry.NewClient(srConfig)

// 	if err != nil {
// 		fmt.Printf("Failed to create schema registry client: %s\n", err)
// 		return nil, nil, err
// 	}
// 	deser, err := k.SchemaType.getDeserializer(src)
// 	return &src, &deser, err
// }

func (k *KafkaCommand) getClientConfig() (*kafka.ConfigMap, error) {
	configMap := kafka.ConfigMap{
		"statistics.interval.ms": 10_000,
	}

	for _, key := range k.Properties.Keys() {
		// Ignore true FROMEARLIEST and FROMLATEST
		if key == "auto.offset.reset" && (k.Earliest || k.Latest) {
			infoLogger.Printf("Ignoring client config entry %s as there is a command line override", key)
		}
		configMap.SetKey(key, k.Properties.MustGet(key))
	}

	if k.Earliest {
		configMap.SetKey("auto.offset.reset", "earliest")
	} else if k.Latest {
		configMap.SetKey("auto.offset.reset", "latest")
	}
	configMap.SetKey("bootstrap.servers", k.BootstrapServer)

	return &configMap, nil
}

func (k *KafkaCommand) setSRClientConfig(param *string, key string) error {
	val, ok := k.SRProperties.Get(key)
	if ok {
		*param = val
	}
	return nil
}

func (k *KafkaCommand) setSRClientConfigInt(param *int, key string) error {
	val, ok := k.SRProperties.Get(key)
	if ok {
		num, err := strconv.Atoi(val)
		if err != nil {
			return err
		}
		*param = num
	}
	return nil
}

func (k *KafkaCommand) getSRClientConfig() (*schemaregistry.Config, error) {
	var ok bool
	configMap := schemaregistry.Config{}
	configMap.SchemaRegistryURL, ok = k.SRProperties.Get("schema.registry.url")
	if ok {
		// configMap.BasicAuthCredentialsSource, _ = k.Properties.Get("basic.auth.credentials.source")
		// configMap.BasicAuthUserInfo, _ = k.Properties.Get("basic.auth.user.info")
		k.setSRClientConfig(&configMap.BasicAuthCredentialsSource, "schema.registry.basic.auth.credentials.source")
		k.setSRClientConfig(&configMap.BasicAuthUserInfo, "schema.registry.basic.auth.user.info")
		k.setSRClientConfig(&configMap.SaslMechanism, "schema.registry.basic.auth.sasl.mechanism")
		k.setSRClientConfig(&configMap.SaslPassword, "schema.registry.basic.auth.sasl.password")
		k.setSRClientConfig(&configMap.SslCaLocation, "ssl.ca.location")
		k.setSRClientConfig(&configMap.SslCertificateLocation, "ssl.certificate.location")
		k.setSRClientConfig(&configMap.SslKeyLocation, "ssl.key.location")
		k.setSRClientConfigInt(&configMap.ConnectionTimeoutMs, "connection.timeout")
		k.setSRClientConfigInt(&configMap.CacheCapacity, "cache.capacity")
		k.setSRClientConfigInt(&configMap.RequestTimeoutMs, "request.timeout")

		return &configMap, nil
	}

	return nil, fmt.Errorf("schema.registry.url is required")
}

func (k *KafkaCommand) validate(ctx context.Context, cmd *cli.Command, subcommand SubCommand) error {
	k.BootstrapServer = cmd.String(BOOTSTRAPSERVER)
	k.ConfigFile = cmd.String(COMMANDCONFIG)
	// TODO Enable Schema Registry processing
	k.SRConfigFile = cmd.String(SRCONFIG)

	// TODO Enable Schema File or SchemaId processing
	k.SchemaFile = cmd.String(SCHEMAFILE)
	k.SchemaId = cmd.Int64(SCHEMAID)
	k.Topic = cmd.String(TOPIC)
	var err error
	if k.SchemaType, err = lookupSchemaType(cmd.String(FORMAT)); err != nil {
		return err
	}
	k.Transactional = cmd.Bool(TRANSACTIONAL)

	if subcommand == Produce {
		k.SubCommand = Produce
		k.SampleData = cmd.String(SAMPLEDATATYPE)
		k.RecordCount = cmd.Int64(RECORDCOUNT)
		k.TPS = cmd.Int64(TPS)

		if k.TPS == 0 {
			return fmt.Errorf("tps cannot be 0")
		}
		if k.TPS < -1 {
			return fmt.Errorf("tps cannot be less than -1")
		}
	} else if subcommand == Consume {
		k.SubCommand = Consume
		k.Earliest = cmd.Bool(FROMEARLIEST)
		k.Latest = cmd.Bool(FROMLATEST)
		if k.Earliest && k.Latest {
			return fmt.Errorf("cannot set both %s and %s", FROMEARLIEST, FROMLATEST)
		}
	} else {
		return fmt.Errorf("%s bad command", subcommand)
	}

	if err := k.SchemaType.validateSchemaParams(k.SchemaFile, k.SchemaId, k.SRConfigFile); err != nil {
		return err
	}

	// TODO Enable Transactional processing
	if k.Transactional {
		return fmt.Errorf("kafka client transaction not enabled")
	}

	if k.ConfigFile != "" {
		k.Properties = properties.MustLoadFile(k.ConfigFile, properties.UTF8)
	} else {
		return fmt.Errorf("configfile is required")
	}

	if k.SRConfigFile != "" {
		k.SRProperties = properties.MustLoadFile(k.SRConfigFile, properties.UTF8)
	}
	return nil
}
