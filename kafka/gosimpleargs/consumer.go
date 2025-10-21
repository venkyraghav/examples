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
	"log"
	"os"
	"os/signal"
	"strings"
	"syscall"
	"time"

	"github.com/confluentinc/confluent-kafka-go/v2/kafka"
	"github.com/confluentinc/confluent-kafka-go/v2/schemaregistry"
	"github.com/confluentinc/confluent-kafka-go/v2/schemaregistry/serde/avro"
	"github.com/urfave/cli/v3"
)

// TODO use context for context and deadlines
func consume(ctx context.Context, cmd *cli.Command) error {
	sigchan := make(chan os.Signal, 1)
	signal.Notify(sigchan, syscall.SIGINT, syscall.SIGTERM)
	kafkaCommand := NewKafkaCommand()

	if err := kafkaCommand.validate(ctx, cmd, Consume); err != nil {
		return err
	}

	var (
		err   error
		c     *kafka.Consumer
		src   schemaregistry.Client
		deser interface{}
		// schemaInfo *schemaregistry.SchemaInfo
	)

	c, err = kafkaCommand.getConsumer()
	if err != nil {
		errorLogger.Fatalf("Failed to create consumer: %s\n", err)
		return err
	}
	infoLogger.Printf("Created Consumer %v\n", c)
	defer c.Close()

	if kafkaCommand.SchemaType.needsDataContract() {
		srConfig, err := kafkaCommand.getSRClientConfig()
		if err != nil {
			return err
		}

		src, err = schemaregistry.NewClient(srConfig)
		if err != nil {
			return err
		}

		deser, err = kafkaCommand.SchemaType.getDeserializer(src)
		if err != nil {
			errorLogger.Printf("Failed to create deserializer: %s\n", err)
			return err
		}
		// _, err = kafkaCommand.getSchemaInfo(src)
		// if err != nil {
		// 	return err
		// }
	}

	topics := strings.Split(kafkaCommand.Topic, ",")
	err = c.SubscribeTopics(topics, nil)
	if err != nil {
		errorLogger.Fatalf("Failed to subscribe to topics: %s\n", err)
		return err
	}

	run := true
	for run {
		select {
		case sig := <-sigchan:
			infoLogger.Printf("Caught signal %v: terminating\n", sig)
			run = false
		default:
			e := c.Poll(100)
			if e == nil {
				continue
			}

			switch ev := e.(type) {
			case *kafka.Message:
				// var value UserAvro
				// value, err := kafkaCommand.SchemaType.getValue(deser, *ev.TopicPartition.Topic, ev.Value)
				// if err != nil {
				// 	fmt.Printf("Failed to deserialize payload: %s\n", err)
				// 	continue
				// }

				// value := UserAvro{}
				var value2 map[string]interface{}
				d := deser.(*avro.GenericDeserializer)
				// err := deser.(*avro.GenericDeserializer).DeserializeInto(*ev.TopicPartition.Topic, ev.Value, &value)
				// value2, err := kafkaCommand.SchemaType.getValue(deser, *ev.TopicPartition.Topic, ev.Value, &value)
				err := d.DeserializeInto(*ev.TopicPartition.Topic, ev.Value, &value2)
				if err != nil {
					fmt.Printf("Failed to deserialize payload: %s\n", err)
				} else {
					fmt.Printf("%% Message on %s:\n%+v\n", ev.TopicPartition, value2)
				}

				if ev.Headers != nil {
					fmt.Printf("%% Headers: %v\n", ev.Headers)
				}

				// We can store the offsets of the messages manually or let
				// the library do it automatically based on the setting
				// enable.auto.offset.store. Once an offset is stored, the
				// library takes care of periodically committing it to the broker
				// if enable.auto.commit isn't set to false (the default is true).
				// By storing the offsets manually after completely processing
				// each message, we can ensure atleast once processing.
				if _, err := c.StoreMessage(ev); err != nil {
					errorLogger.Printf("%% Error storing offset after message %s:\n", ev.TopicPartition)
					return err
				}
			case *kafka.Stats:
				GetPrometheusStatistics().HandleStats(ev.String())
			// case kafka.AssignedPartitions:
			// 	c.Assign(ev.Partitions)
			// case kafka.RevokedPartitions:
			// 	c.Unassign()
			case kafka.OffsetsCommitted:
				if ev.Error != nil {
					log.Printf("Offset commit failed: %v", ev.Error)
				} else {
					log.Printf("Offsets committed: %v", ev.Offsets)
				}
			case kafka.Error:
				// Errors should generally be considered
				// informational, the client will try to
				// automatically recover.
				// But in this example we choose to terminate
				// the application if all brokers are down.
				if ev.Code() == kafka.ErrUnknownTopic || ev.Code() == kafka.ErrUnknownTopicID || ev.Code() == kafka.ErrUnknownTopicOrPart {
					// Recoverable. Quit probably misconfigured or typo on topic
					// Topic/partition not found Recoverable (if topic auto-creation enabled)
					run = false
					errorLogger.Printf("Handle event: %s. Quitting\n", ev)
					return ev
				} else if ev.IsFatal() {
					// Unrecoverable. Save State of the records produced for retransmission and quit

					errorLogger.Printf("Handle event: %s. Quitting\n", ev)
					return ev
				} else if ev.IsRetriable() || ev.IsTimeout() {
					// Recoverable. Save State of the records produced for retransmission and quit
					// Or keep program alive and alert

					infoLogger.Printf("Handle event: %s. Sleeping for 10 seconds\n", ev)
					time.Sleep(time.Second * 10)
				} else if ev.TxnRequiresAbort() {
					errorLogger.Printf("Handle event: %s. Abort the producer transactions\n", ev)
				} else {
					infoLogger.Printf("Unhandled event: %s\n", ev)
				}
			default:
				errorLogger.Printf("Ignored %v\n", e)
			}
		}
	}

	infoLogger.Printf("Closing consumer\n")
	// time.Sleep(time.Minute * 2)
	return nil
}
