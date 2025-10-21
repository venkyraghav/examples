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
	"os"
	"os/signal"
	"syscall"
	"time"

	"github.com/confluentinc/confluent-kafka-go/v2/kafka"
)

func produce(configMap *kafka.ConfigMap, command KafkaCommand) error {
	sigchan := make(chan os.Signal, 1)
	signal.Notify(sigchan, syscall.SIGINT, syscall.SIGTERM)

	p, err := kafka.NewProducer(configMap)

	if err != nil {
		errorLogger.Fatalf("Failed to create producer: %s\n", err)
		return err
	}

	infoLogger.Printf("Created Producer %v\n", p)

	// Listen to all the events on the default events channel
	go func() {
		for e := range p.Events() {
			switch ev := e.(type) {
			case *kafka.Message:
				// The message delivery report, indicating success or permanent failure after retries have been exhausted.
				// Application level retries won't help since the client is already configured to do that.
				m := ev
				if m.TopicPartition.Error != nil {
					errorLogger.Printf("Delivery failed: %v\n", m.TopicPartition.Error)
				} else {
					infoLogger.Printf("Delivered message to topic %s [%d] at offset %v\n",
						*m.TopicPartition.Topic, m.TopicPartition.Partition, m.TopicPartition.Offset)
				}
			case *kafka.Stats:
				GetPrometheusStatistics().HandleStats(ev.String())
			case kafka.Error:
				// Generic client instance-level errors, such as broker connection failures, authentication issues, etc.
				// These errors should generally be considered informational as the underlying client will automatically try to
				// recover from any errors encountered, the application does not need to take action on them.
				errorLogger.Printf("Error: %v\n", ev)

				if ev.Code() == kafka.ErrUnknownTopic || ev.Code() == kafka.ErrUnknownTopicID || ev.Code() == kafka.ErrUnknownTopicOrPart {
					// Recoverable. Quit probably misconfigured or typo on topic
					// Topic/partition not found Recoverable (if topic auto-creation enabled)

					errorLogger.Printf("Handle event: %s. Quitting\n", ev)
					os.Exit(int(ev.Code()))
				} else if ev.IsFatal() {
					// Unrecoverable. Save State of the records produced for retransmission and quit

					errorLogger.Printf("Handle event: %s. Quitting\n", ev)
					os.Exit(int(ev.Code()))
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
				errorLogger.Printf("Ignored event: %s\n", ev)
			}
		}
	}()

	msgcnt := 0
	for msgcnt < command.RecordCount {
		value := fmt.Sprintf("Producer example, message #%d", msgcnt)
		err = p.Produce(&kafka.Message{
			TopicPartition: kafka.TopicPartition{Topic: &command.Topic, Partition: kafka.PartitionAny},
			Value:          []byte(value),
			Headers:        []kafka.Header{{Key: "myTestHeader", Value: []byte("header values are binary")}},
		}, nil)

		if err != nil {
			if err.(kafka.Error).Code() == kafka.ErrQueueFull {
				// Producer queue is full, wait 1s for messages
				// to be delivered then try again.
				time.Sleep(time.Second)
				continue
			}
			errorLogger.Printf("Failed to produce message: %v\n", err)
		} else {
			if command.TPS > 0 {
				time.Sleep(time.Millisecond * time.Duration(1000/command.TPS))
			}
		}
		msgcnt++
	}

	// Flush and close the producer and the events channel
	for p.Flush(10000) > 0 {
		infoLogger.Print("Still waiting to flush outstanding messages\n")
	}
	p.Close()
	var t = 30
	infoLogger.Printf("Sleeping for %d seconds(s)", t)
	time.Sleep(time.Second * time.Duration(t))
	return nil
}
