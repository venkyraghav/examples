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

	"github.com/confluentinc/confluent-kafka-go/v2/kafka"
	"github.com/magiconair/properties"
)

var (
	errorLogger, infoLogger *log.Logger
)

func main() {
	errorLogger = log.New(os.Stderr, "ERROR: ", log.LstdFlags|log.Lshortfile)
	infoLogger = log.New(os.Stderr, "INFO: ", log.LstdFlags|log.Lshortfile)

	if len(os.Args) != 4 {
		fmt.Fprintf(os.Stderr, "Usage: %s {produce|consume} <config-file> <topic>\n", os.Args[0])
		os.Exit(1)
	}
	command := os.Args[1]
	configFile := os.Args[2]
	topic := os.Args[3]

	GetPrometheusStatistics()
	props := properties.MustLoadFile(configFile, properties.UTF8)
	configMap := &kafka.ConfigMap{
		"statistics.interval.ms": 10000,
	}
	for _, key := range props.Keys() {
		configMap.SetKey(key, props.MustGet(key))
	}

	if command == "produce" {
		produce(configMap, topic)
	} else if command == "consume" {
		consume(configMap, topic)
	} else {
		errorLogger.Fatalf("Unknown command %s", command)
	}
}
