package main

import (
	"fmt"
	"log"
	"time"

	"github.com/xitongsys/parquet-go-source/local"
	"github.com/xitongsys/parquet-go/parquet"
	"github.com/xitongsys/parquet-go/reader"
	"github.com/xitongsys/parquet-go/writer"
)

// Record is the schema for Parquet rows.
// Use parquet tags to define column names, repetition and types.
type Record struct {
	// Basic scalar types
	ID    int64   `parquet:"name=id, type=INT64"`
	Name  string  `parquet:"name=name, type=BYTE_ARRAY, convertedtype=UTF8, encoding=PLAIN_DICTIONARY"`
	Score float64 `parquet:"name=score, type=DOUBLE"`

	// Timestamp (store as INT64 epoch millis)
	CreatedAt int64 `parquet:"name=created_at, type=INT64"`

	// Repeated field (array of strings)
	Tags []string `parquet:"name=tags, type=BYTE_ARRAY, convertedtype=UTF8, repetitiontype=REPEATED"`
}

func main() {
	const fname = "sample.parquet"

	// 1) Create local file writer
	fw, err := local.NewLocalFileWriter(fname)
	if err != nil {
		log.Fatalf("Failed to create file writer: %v", err)
	}
	// 2) Create Parquet writer with schema = Record
	pw, err := writer.NewParquetWriter(fw, new(Record), 4) // 4 concurrent writers
	if err != nil {
		log.Fatalf("Failed to create parquet writer: %v", err)
	}
	// Set compression (SNAPPY recommended)
	pw.CompressionType = parquet.CompressionCodec_SNAPPY

	// 3) Write some example rows
	now := time.Now()
	for i := 0; i < 10; i++ {
		rec := Record{
			ID:        int64(i + 1),
			Name:      fmt.Sprintf("User-%02d", i+1),
			Score:     100.0 - float64(i)*3.14,
			CreatedAt: now.Add(time.Duration(i) * time.Hour).UnixMilli(),
			Tags:      []string{"demo", fmt.Sprintf("tag-%d", i%3)},
		}
		if err = pw.Write(rec); err != nil {
			log.Fatalf("Write error: %v", err)
		}
	}

	// 4) Finalize writer and close file
	if err = pw.WriteStop(); err != nil {
		log.Fatalf("WriteStop error: %v", err)
	}
	if err = fw.Close(); err != nil {
		log.Fatalf("File close error: %v", err)
	}
	fmt.Printf("Parquet file written: %s\n", fname)

	// ---------------------------
	// Simple read-back example
	// ---------------------------
	fr, err := local.NewLocalFileReader(fname)
	if err != nil {
		log.Fatalf("Failed to open parquet file for reading: %v", err)
	}
	defer fr.Close()

	pr, err := reader.NewParquetReader(fr, new(Record), 4)
	if err != nil {
		log.Fatalf("Failed to create parquet reader: %v", err)
	}
	defer pr.ReadStop()

	num := int(pr.GetNumRows())
	fmt.Printf("Number of rows in file: %d\n", num)

	// read in batches
	rows := make([]Record, 0)
	if err = pr.Read(&rows); err != nil {
		log.Fatalf("Read error: %v", err)
	}
	for i, r := range rows {
		fmt.Printf("Row %d: %+v\n", i+1, r)
	}
}
