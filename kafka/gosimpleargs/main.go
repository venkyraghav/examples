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

	"github.com/urfave/cli/v3"
)

var (
	errorLogger, infoLogger         *log.Logger
	programName, version, buildDate string
	COPYRIGHT                       = `Copyright 2025 Confluent Inc.
Licensed under the Apache License, Version 2.0 (the "License");you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, 
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and limitations under the License.`
	BOOTSTRAPSERVER = "bootstrap-server"
	COMMANDCONFIG   = "command-config"
	SRCONFIG        = "sr-config"
	SCHEMAFILE      = "schema-file"
	SCHEMAID        = "schema-id"
	TOPIC           = "topic"
	FORMAT          = "format"
	SAMPLEDATATYPE  = "sample-datatype"
	RECORDCOUNT     = "record-count"
	TPS             = "tps"
	TRANSACTIONAL   = "transactional"
	FROMEARLIEST    = "from-earliest"
	FROMLATEST      = "from-latest"
)

type Author struct {
	Name    string `json:"name"`
	Email   string `json:"email"`
	Company string `json:"company"`
	Github  string `json:"github"`
}

func main() {
	errorLogger = log.New(os.Stderr, "ERROR: ", log.LstdFlags|log.Lshortfile)
	infoLogger = log.New(os.Stderr, "INFO: ", log.LstdFlags|log.Lshortfile)

	GetPrometheusStatistics()

	cmd := &cli.Command{
		Name:      programName,
		Version:   version,
		Copyright: COPYRIGHT,
		Usage:     "Demonstrates Kafka Producer and Consumer Code, Error Handling, and Statistics processing",
		Suggest:   true,
		Metadata: map[string]interface{}{
			"Build Date": buildDate,
		},
		Authors: []any{
			Author{Name: "Venky Narayanan", Email: "venkyraghav@gmail.com", Company: "Confluent.IO", Github: "https://github.com/venkyraghav"},
		},
		Commands: []*cli.Command{
			{
				Name:    "produce",
				Aliases: []string{"p"},
				Usage:   "Demonstrate Kafka Producer",
				Suggest: true,
				Flags: []cli.Flag{
					&cli.StringFlag{Name: BOOTSTRAPSERVER, Aliases: []string{"b"}, Usage: "Bootstrap Server", Required: true},
					&cli.StringFlag{Name: COMMANDCONFIG, Aliases: []string{"c"}, Usage: "Command Config File", Required: false},
					&cli.StringFlag{Name: SRCONFIG, Aliases: []string{"s"}, Usage: "SchemaRegistry Config File", Required: false, Category: "Schema"},
					&cli.StringFlag{Name: TOPIC, Aliases: []string{"t"}, Usage: "Topic Name", Required: true},
					&cli.StringFlag{Name: FORMAT, Aliases: []string{"f"}, Usage: "Data Format. STRING, JSON, AVRO, PROTOBUF, BYTE", Required: false, Value: "STRING", Category: "Schema"},
					&cli.StringFlag{Name: SAMPLEDATATYPE, Aliases: []string{"d"}, Usage: "Sample Data Type. console-input, users, pageviews", Required: false, Value: "users", Category: "Datagen"},
					&cli.IntFlag{Name: RECORDCOUNT, Aliases: []string{"n"}, Usage: "# of records", Required: false, Value: 100, Category: "Datagen"},
					&cli.IntFlag{Name: TPS, Aliases: []string{"p"}, Usage: "Transaction per second. -1 = no throttle", Required: false, Value: -1, Category: "Datagen"},
					&cli.BoolFlag{Name: TRANSACTIONAL, Aliases: []string{"X"}, Usage: "Transactional Producer", Required: false, Value: false},
				},
				MutuallyExclusiveFlags: []cli.MutuallyExclusiveFlags{
					cli.MutuallyExclusiveFlags{
						Flags: [][]cli.Flag{
							[]cli.Flag{
								&cli.StringFlag{Name: SCHEMAFILE, Usage: "Schema File", Required: false},
								&cli.IntFlag{Name: SCHEMAID, Usage: "SchemaRegistry Schema ID", Required: false, Value: 0},
							},
						},
						Required: false,
						Category: "Schema",
					},
				},
				Action: produce,
			},
			{
				Name:    "consume",
				Aliases: []string{"c"},
				Usage:   "Demonstrate Kafka Consumer",
				Suggest: true,
				Flags: []cli.Flag{
					&cli.StringFlag{Name: BOOTSTRAPSERVER, Aliases: []string{"b"}, Usage: "Bootstrap Server", Required: true},
					&cli.StringFlag{Name: COMMANDCONFIG, Aliases: []string{"c"}, Usage: "Command Config File", Required: false},
					&cli.StringFlag{Name: SRCONFIG, Aliases: []string{"s"}, Usage: "SchemaRegistry Config File", Required: false, Category: "Schema"},
					&cli.StringFlag{Name: TOPIC, Aliases: []string{"t"}, Usage: "Topic Name", Required: true},
					&cli.StringFlag{Name: FORMAT, Aliases: []string{"f"}, Usage: "Data Format. STRING, JSON, AVRO, PROTOBUF, BYTE", Required: false, Value: "STRING", Category: "Schema"},
					&cli.BoolFlag{Name: TRANSACTIONAL, Aliases: []string{"X"}, Usage: "Transactional Producer", Required: false, Value: false},
				},
				MutuallyExclusiveFlags: []cli.MutuallyExclusiveFlags{
					cli.MutuallyExclusiveFlags{
						Flags: [][]cli.Flag{
							[]cli.Flag{
								&cli.StringFlag{Name: SCHEMAFILE, Usage: "Schema File", Required: false},
								&cli.IntFlag{Name: SCHEMAID, Usage: "SchemaRegistry Schema ID", Required: false, Value: 0},
							},
						},
						Required: false,
						Category: "Schema",
					},
					cli.MutuallyExclusiveFlags{
						Flags: [][]cli.Flag{
							[]cli.Flag{
								&cli.BoolFlag{Name: FROMEARLIEST, Aliases: []string{"e"}, Usage: "From Earliest", Required: false, Value: false},
								&cli.BoolFlag{Name: FROMLATEST, Aliases: []string{"l"}, Usage: "From Latest", Required: false, Value: false},
							},
						},
						Required: false,
						Category: "Offset",
					},
				},
				Action: consume,
			},
		},
		EnableShellCompletion: true,
		HideHelp:              false,
		HideVersion:           false,
		OnUsageError: func(ctx context.Context, cmd *cli.Command, err error, isSubcommand bool) error {
			if isSubcommand {
				return err
			}
			fmt.Fprintf(cmd.Root().Writer, "WRONG: %#v\n", err)
			return nil
		},
	}

	if err := cmd.Run(context.Background(), os.Args); err != nil {
		log.Fatal(err)
	}
}
