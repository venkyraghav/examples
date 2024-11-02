package examples.clients;

import java.io.IOException;
import java.util.Properties;
import java.util.Random;

import org.apache.avro.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;

import examples.Myrecordkeyproto;
import examples.Myrecordproto;
import examples.common.Format;
import examples.common.ProducerCommand;

public abstract class ProducerBase {
    private static final Logger log = LoggerFactory.getLogger(ProducerBase.class);
    protected ProducerCommand command;
    private org.apache.avro.Schema.Parser parser;
    private String avroKeySchemaString = "{\"type\":\"record\",\"name\":\"mykey\",\"fields\":[{\"name\":\"key\",\"type\":\"string\"}]}";
    private String avroValueSchemaString = "{\"type\":\"record\",\"name\":\"myrecord\",\"fields\":[{\"name\":\"f1\",\"type\":\"string\"}]}";
    private Schema avroKeySchema;
    private Schema avroValueSchema;

    public ProducerBase(ProducerCommand command) {
        this.command = command;
        this.parser = new org.apache.avro.Schema.Parser();

        avroKeySchema = parser.parse(avroKeySchemaString);
        avroValueSchema = parser.parse(avroValueSchemaString);
    }

    public void run() throws Exception {
        Properties props = command.getProperties();
        props.forEach(
                (k, v) -> {
                    if (log.isDebugEnabled()) {
                        log.debug("Key=" + k + ", Value=" + v);
                    }
                });
        doProcess(props);
    }

    protected abstract void doProcess(Properties props) throws IOException;

    protected Object getKey(Format format, long payloadSize) {
        String fieldName = "key";
        return switch (format) {
            case STRING -> generateRandomString(payloadSize);
            case AVRO -> generateAvroPayload(avroKeySchema, fieldName, payloadSize);
            case JSON -> generateJsonPayload(fieldName, payloadSize);
            case JSON_SR -> generateJsonSchemaKeyPayload(payloadSize);
            case PROTOBUF -> generateProtobufKeyPayload(payloadSize);
            default -> null;
        };
    }

    protected Object getValue(Format format, long payloadSize) {
        String fieldName = "f1";
        return switch (format) {
            case STRING -> generateRandomString(payloadSize);
            case AVRO -> generateAvroPayload(avroValueSchema, fieldName, payloadSize);
            case JSON -> generateJsonPayload(fieldName, payloadSize);
            case JSON_SR -> generateJsonSchemaValuePayload(payloadSize);
            case PROTOBUF -> generateProtobufValuePayload(payloadSize);
            default -> null;
        };
    }

    private Object generateProtobufKeyPayload(long payloadSize) {
        return Myrecordkeyproto.myrecordkeyproto.newBuilder().setKey(generateRandomString(payloadSize)).build();
    }

    private Object generateProtobufValuePayload(long payloadSize) {
        return Myrecordproto.myrecordproto.newBuilder().setF1(generateRandomString(payloadSize)).build();
    }

    static class MyrecordJsonSr {
        @JsonProperty
        public String f1;

        public MyrecordJsonSr() {}

        public MyrecordJsonSr(String f1) {
            this.f1 = f1;
        }
    }

    static class MyrecordkeyJsonSr {
        @JsonProperty
        public String key;

        public MyrecordkeyJsonSr() {}

        public MyrecordkeyJsonSr(String key) {
            this.key = key;
        }
    }

    private Object generateJsonSchemaValuePayload(long payloadSize) {
        return new MyrecordJsonSr(generateRandomString(payloadSize));
    }

    private Object generateJsonSchemaKeyPayload(long payloadSize) {
        return new MyrecordkeyJsonSr(generateRandomString(payloadSize));
    }

    private Object generateJsonPayload(String fieldName, long payloadSize) {
        return "{\"" + fieldName + "\":\"" + generateRandomString(payloadSize) + "\"}";
    }

    private Object generateAvroPayload(Schema avroSchema, String fieldName, long payloadSize) {
        org.apache.avro.generic.GenericRecord avroRecord = new org.apache.avro.generic.GenericData.Record(avroSchema);
        avroRecord.put(fieldName, generateRandomString(payloadSize));
        return avroRecord;
    }

    protected String generateRandomString(long targetStringLength) {
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        Random random = new Random();

        return random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }
}
