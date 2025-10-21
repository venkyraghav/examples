package main

import (
	"fmt"
	"os"
	"strings"

	"github.com/confluentinc/confluent-kafka-go/v2/schemaregistry"
	"github.com/confluentinc/confluent-kafka-go/v2/schemaregistry/serde"
	"github.com/confluentinc/confluent-kafka-go/v2/schemaregistry/serde/avro"
	"github.com/confluentinc/confluent-kafka-go/v2/schemaregistry/serde/jsonschema"
	"github.com/confluentinc/confluent-kafka-go/v2/schemaregistry/serde/protobuf"
	hambaavro "github.com/hamba/avro/v2"
)

type SchemaType int

const (
	_ SchemaType = iota
	SchemaTypeByte
	SchemaTypeLong
	SchemaTypeInt
	SchemaTypeFloat
	SchemaTypeDouble
	SchemaTypeString
	SchemaTypeJsonschema
	SchemaTypeProtobuf
	SchemaTypeAvro
	SchemaTypeJson
)

func bytesToInt64(data []byte) int64 {
	var result int64
	for _, b := range data {
		result = (result << 8) | int64(b)
	}
	return result
}

func bytesToInt32(data []byte) int32 {
	var result int32
	for _, b := range data {
		result = (result << 4) | int32(b)
	}
	return result
}

func (st SchemaType) getValue(deser interface{}, topicName string, data []byte, value interface{}) (interface{}, error) {
	switch st {
	case SchemaTypeByte:
		return data, nil
	case SchemaTypeString, SchemaTypeJson:
		return string(data), nil
	case SchemaTypeLong:
		return bytesToInt64(data), nil
	case SchemaTypeInt:
		return bytesToInt32(data), nil
	case SchemaTypeFloat:
		return float32(bytesToInt32(data)), nil
	case SchemaTypeDouble:
		return float64(bytesToInt64(data)), nil
	case SchemaTypeJsonschema, SchemaTypeProtobuf:
		return nil, fmt.Errorf("not implements %s", st.String())
	case SchemaTypeAvro:
		d := deser.(*avro.GenericDeserializer)
		err := d.DeserializeInto(topicName, data, &value)
		if err != nil {
			return nil, err
		}
		fmt.Printf("In getValue Message on %+v\n", value)
		return value, nil
	}
	return nil, fmt.Errorf("unsupported schemaType %s", st.String())
}

func (st SchemaType) getDeserializer(src schemaregistry.Client) (interface{}, error) {
	if st.needsDataContract() {
		switch st {
		case SchemaTypeAvro:
			return avro.NewGenericDeserializer(src, serde.ValueSerde, avro.NewDeserializerConfig())
		case SchemaTypeJsonschema:
			return jsonschema.NewDeserializer(src, serde.ValueSerde, jsonschema.NewDeserializerConfig())
		case SchemaTypeProtobuf:
			return protobuf.NewDeserializer(src, serde.ValueSerde, protobuf.NewDeserializerConfig())
		}
	}
	return nil, fmt.Errorf("%s bad format", st.String())
}

func (st SchemaType) needsDataContract() bool {
	switch st {
	case SchemaTypeByte, SchemaTypeLong, SchemaTypeInt, SchemaTypeFloat, SchemaTypeDouble, SchemaTypeString, SchemaTypeJson:
		return false
	case SchemaTypeAvro, SchemaTypeJsonschema, SchemaTypeProtobuf:
		return true
	default:
		return false
	}
}

func (st SchemaType) loadSchema(schemaFile string) (*schemaregistry.SchemaInfo, error) {
	if st.needsDataContract() {
		if schemaBytes, err := os.ReadFile(schemaFile); err != nil {
			return nil, err
		} else {
			switch st {
			case SchemaTypeAvro:
				if _, err := hambaavro.Parse(string(schemaBytes)); err != nil {
					return nil, err
				}
			}
			return &schemaregistry.SchemaInfo{Schema: string(schemaBytes)}, nil
		}
	}
	return nil, nil
}

func (st SchemaType) validateSchemaParams(schemaFile string, schemaId int64, srConfigFile string) error {
	if st.needsDataContract() {
		if srConfigFile == "" {
			return fmt.Errorf("missing parameter %s for %s format", SRCONFIG, st.String())
		}
		if schemaFile == "" && schemaId < 1 {
			return fmt.Errorf("%s or %s is required for %s format", SCHEMAFILE, SCHEMAID, st.String())
		} else if schemaFile != "" && schemaId > 0 {
			return fmt.Errorf("both %s and %s can not be provided for %s format", SCHEMAFILE, SCHEMAID, st.String())
		}
		// TODO Enable AVRO JSON PROTOBUF BYTE Formats
		// return fmt.Errorf("%s format support is not enabled", st.String())
	} else {
		var props []string
		if srConfigFile != "" {
			props = append(props, SRCONFIG)
		}
		if schemaFile != "" {
			props = append(props, SCHEMAFILE)
		}
		if schemaId > 0 {
			props = append(props, SCHEMAID)
		}
		if len(props) > 0 {
			return fmt.Errorf("%s param not required for %s format", strings.Join(props, ","), st.String())
		}
	}
	return nil
}

func lookupSchemaType(schemaType string) (SchemaType, error) {
	switch strings.ToLower(schemaType) {
	case "byte":
		return SchemaTypeByte, nil
	case "long":
		return SchemaTypeLong, nil
	case "int":
		return SchemaTypeInt, nil
	case "float":
		return SchemaTypeFloat, nil
	case "double":
		return SchemaTypeDouble, nil
	case "string":
		return SchemaTypeString, nil
	case "jsonschema":
		return SchemaTypeJsonschema, nil
	case "proto":
		return SchemaTypeProtobuf, nil
	case "avro":
		return SchemaTypeAvro, nil
	case "json":
		return SchemaTypeJson, nil
	}
	return 0, fmt.Errorf("invalid schematype %s", schemaType)
}

func (st SchemaType) String() string {
	switch st {
	case SchemaTypeByte:
		return "byte"
	case SchemaTypeLong:
		return "long"
	case SchemaTypeInt:
		return "int"
	case SchemaTypeFloat:
		return "float"
	case SchemaTypeDouble:
		return "double"
	case SchemaTypeString:
		return "string"
	case SchemaTypeJsonschema:
		return "jsonschema"
	case SchemaTypeProtobuf:
		return "proto"
	case SchemaTypeAvro:
		return "avro"
	case SchemaTypeJson:
		return "json"
	default:
		return "unknown"
	}
}
