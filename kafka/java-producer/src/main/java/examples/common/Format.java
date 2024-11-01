package examples.common;

import java.util.ArrayList;
import java.util.List;

public enum Format {
    STRING, AVRO, JSON, JSON_SR, PROTOBUF;

    public boolean isSupported() {
        return switch (this) {
            case STRING, AVRO, JSON -> true;
            default -> false;
        };
    }

    public String getSerializer() throws ArgException {
        return switch (this) {
            case JSON, STRING -> "org.apache.kafka.common.serialization.StringSerializer";
            case AVRO -> "io.confluent.kafka.serializers.KafkaAvroSerializer";
            default -> throw new ArgException("Unsupported format");
        };
    }

    public String toString() {
        return this.name().toLowerCase();
    }

    public boolean isSchemaRegistryRequired() {
        return switch (this) {
            case AVRO, JSON_SR, PROTOBUF -> true;
            default -> false;
        };
    }

    public static Format lookup(String format) {
        return Format.valueOf(format.toUpperCase());
    }

    public static List<String> supportedFormats() {
        List<String> supported = new ArrayList<String>();
        for (Format format : Format.values()) {
            if (format.isSupported()) {
                supported.add(format.toString());
            }
        }
        return supported;
    }
}