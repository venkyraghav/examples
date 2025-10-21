package main

type SampleData struct {
	SchemaType
	SampleDatatype string
	NewFunc        func() interface{}
}

var sampleDataList = []SampleData{
	// SampleData{SchemaType: SchemaTypeAvro, SampleDatatype: "user", NewFunc: func() interface{} { return &UserAvro{} }},
	// SampleData{SchemaType: SchemaTypeProtobuf, SampleDatatype: "user", NewFunc: func() interface{} { return &UserProto{} }},
}
