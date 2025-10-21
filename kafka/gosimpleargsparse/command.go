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
	"fmt"
	"log"
	"os"
	"strconv"

	"github.com/akamensky/argparse"
	"github.com/confluentinc/confluent-kafka-go/schemaregistry"
	"github.com/confluentinc/confluent-kafka-go/v2/kafka"
	"github.com/magiconair/properties"
)

type SubCommand string

type DataFormat string

const (
	Produce SubCommand = "produce"
	Consume SubCommand = "consume"

	String   DataFormat = "STRING"
	Avro     DataFormat = "AVRO"
	Json     DataFormat = "JSON"
	Protobuf DataFormat = "PROTOBUF"
)

type KafkaCommand struct {
	SubCommand
	Format          DataFormat
	BootstrapServer string
	ConfigFile      string
	SRConfigFile    string
	SchemaFile      string
	Topic           string
	Transactional   bool

	SampleData  string
	RecordCount int
	TPS         int
	Earliest    bool
	Latest      bool

	Properties *properties.Properties
}

func (k *KafkaCommand) String() string {
	return fmt.Sprintf("SubCommand: %s, Format: %s, BootstrapServer: %s, ConfigFile: %s, SRConfigFile: %s, Topic: %s, SampleData: %s, RecordCount: %d, TPS: %d, Earliest: %v, Latest: %v",
		k.SubCommand, k.Format,
		k.BootstrapServer, k.ConfigFile, k.SRConfigFile, k.Topic,
		k.SampleData, k.RecordCount, k.TPS,
		k.Earliest, k.Latest)
}

func (k *KafkaCommand) getClientConfig() (kafka.ConfigMap, error) {
	configMap := kafka.ConfigMap{
		"statistics.interval.ms": 10000,
	}

	for _, key := range k.Properties.Keys() {
		configMap.SetKey(key, k.Properties.MustGet(key))
	}

	configMap.SetKey("bootstrap.servers", k.BootstrapServer)

	return configMap, nil
}

func (k *KafkaCommand) setSRClientConfig(param *string, key string) error {
	val, ok := k.Properties.Get(key)
	if ok {
		*param = val
	}
	return nil
}

func (k *KafkaCommand) setSRClientConfigInt(param *int, key string) error {
	val, ok := k.Properties.Get(key)
	if ok {
		num, err := strconv.Atoi(val)
		if err != nil {
			return fmt.Errorf("Error:", err)
		}
		*param = num
	}
	return nil
}

func (k *KafkaCommand) getSRClientConfig() (schemaregistry.Config, bool) {
	var ok bool
	configMap := schemaregistry.Config{}
	configMap.SchemaRegistryURL, ok = k.Properties.Get("schema.registry.url")
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

		return configMap, true
	}

	return configMap, false
}

func (k *KafkaCommand) getProducerConfig() (kafka.ConfigMap, error) {
	// TODO remove irrelevant producer config entries
	return k.getClientConfig()
}

func (k *KafkaCommand) Validate() error {
	parser := argparse.NewParser(PROGNAME, "Kafka Datagen Producer and Consumer")

	producer := parser.NewCommand("produce", "Produce records to Kafka")
	consumer := parser.NewCommand("consume", "Consume records from Kafka")

	format := parser.String("f", "format", &argparse.Options{Help: "Data Format. STRING, JSON, AVRO, PROTOBUF, BYTE", Default: "STRING"})

	bootstrapServer := parser.String("b", "bootstrap-server", &argparse.Options{Required: true, Help: "Bootstrap Server"})
	configFile := parser.String("c", "command-config", &argparse.Options{Required: false, Help: "Command Config File"})
	srConfigFile := parser.String("s", "sr-config", &argparse.Options{Required: false, Help: "Schema Registry Config File"})
	schemaFile := parser.String("", "schema-file", &argparse.Options{Required: false, Help: "Schema File"})
	topic := parser.String("t", "topic", &argparse.Options{Required: true, Help: "Topic Name"})

	sampleDataType := producer.String("d", "sample-datatype", &argparse.Options{Required: false, Help: "Sample Data Type. users, pageviews", Default: "users"})
	recordCount := producer.Int("n", "record-count", &argparse.Options{Required: false, Help: "# of records", Default: 100})
	tps := producer.Int("p", "tps", &argparse.Options{Required: false, Help: "Transaction per second. -1 = no throttle", Default: -1})
	transactional_producer := producer.Flag("X", "transactional", &argparse.Options{Required: false, Help: "Transactional", Default: false})

	earliest := consumer.Flag("e", "from-earliest", &argparse.Options{Required: false, Help: "From Earliest", Default: false})
	latest := consumer.Flag("l", "from-latest", &argparse.Options{Required: false, Help: "From Latest", Default: true})
	transactional_consumer := consumer.Flag("X", "transactional", &argparse.Options{Required: false, Help: "Transactional", Default: false})

	if err := parser.Parse(os.Args); err != nil {
		return fmt.Errorf("%s", parser.Usage(err))
	}

	k.Format = DataFormat(*format)
	k.BootstrapServer = *bootstrapServer
	k.ConfigFile = *configFile
	k.SRConfigFile = *srConfigFile
	k.SchemaFile = *schemaFile
	k.Topic = *topic
	k.Transactional = (*transactional_producer || *transactional_consumer)

	switch {
	case producer.Happened():
		k.SubCommand = Produce
		k.SampleData = *sampleDataType
		k.RecordCount = *recordCount
		k.TPS = *tps
		if k.TPS == 0 {
			return fmt.Errorf("tps cannot be 0")
		}
		if k.TPS < -1 {
			return fmt.Errorf("tps cannot be less than -1")
		}
	case consumer.Happened():
		k.SubCommand = Consume
		k.Earliest = *earliest
		k.Latest = *latest
	default:
		return fmt.Errorf("%s", parser.Usage("bad command"))
	}

	switch k.Format {
	case String:
		// do nothing
	case Avro, Json, Protobuf:
		return fmt.Errorf("%s format support is not enabled", k.Format)
	default:
		return fmt.Errorf("%s", parser.Usage("bad format"))
	}

	if k.Transactional {
		return fmt.Errorf("kafka client transaction not enabled")
	}

	log.Printf("k => %s\n", k.String())
	k.Properties = properties.MustLoadFile(k.ConfigFile, properties.UTF8)

	return nil
}

func (k *KafkaCommand) Process() error {
	configMap, err := k.getClientConfig()
	if err != nil {
		return err
	}
	switch k.SubCommand {
	case Produce:

		return produce(&configMap, *k)
	case Consume:
		return consume(&configMap, *k)
	default:
		return fmt.Errorf("bad subcommand")
	}
}
