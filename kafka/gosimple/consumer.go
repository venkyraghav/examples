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
	"strings"
	"syscall"
	"time"

	"github.com/confluentinc/confluent-kafka-go/v2/kafka"
)

func consume(configMap *kafka.ConfigMap, topic string) {
	sigchan := make(chan os.Signal, 1)
	signal.Notify(sigchan, syscall.SIGINT, syscall.SIGTERM)

	c, err := kafka.NewConsumer(configMap)

	if err != nil {
		fmt.Fprintf(os.Stderr, "Failed to create consumer: %s\n", err)
		os.Exit(1)
	}

	infoLogger.Printf("Created Consumer %v\n", c)

	topics := strings.Split(topic, ",")
	err = c.SubscribeTopics(topics, nil)

	if err != nil {
		fmt.Fprintf(os.Stderr, "Failed to subscribe to topics: %s\n", err)
		os.Exit(1)
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
				// Process the message received.
				infoLogger.Printf("%% Message on %s:\n%s\n", ev.TopicPartition, string(ev.Value))
				if ev.Headers != nil {
					infoLogger.Printf("%% Headers: %v\n", ev.Headers)
				}

				// We can store the offsets of the messages manually or let
				// the library do it automatically based on the setting
				// enable.auto.offset.store. Once an offset is stored, the
				// library takes care of periodically committing it to the broker
				// if enable.auto.commit isn't set to false (the default is true).
				// By storing the offsets manually after completely processing
				// each message, we can ensure atleast once processing.
				_, err := c.StoreMessage(ev)
				if err != nil {
					fmt.Fprintf(os.Stderr, "%% Error storing offset after message %s:\n", ev.TopicPartition)
				}
			case *kafka.Stats:
				GetPrometheusStatistics().HandleStats(ev.String())
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
				errorLogger.Printf("Ignored %v\n", e)
			}
		}
	}

	infoLogger.Printf("Closing consumer\n")
	c.Close()
	time.Sleep(time.Minute * 2)
}
