package com.venkyraghav.kafka.connect.transforms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;
import org.junit.jupiter.api.Test;

class ExtractDecodedPayloadTest {
    @Test
    void shouldAddHeadersFromDecodedJsonFields() {
        ExtractDecodedPayload.Value<SourceRecord> xform =
            new ExtractDecodedPayload.Value<>();

        xform.configure(Map.of(
            "field", "payload",
            "topic.format.mapping", "orders.json:json",
            "default.format", "json",
            "json.header.fields", "metadata.correlationId,metadata.source,eventType",
            "json.header.names", "correlationId,source,eventType",
            "fail.on.invalid", "true"
        ));

        String decodedJson = """
            {
            "eventType": "ORDER_CREATED",
            "metadata": {
                "correlationId": "abc-123",
                "source": "crm"
            },
            "orderId": 101
            }
            """;

        String b64 = Base64.getEncoder()
            .encodeToString(decodedJson.getBytes(StandardCharsets.UTF_8));

        Schema inputSchema = SchemaBuilder.struct()
            .field("payload", Schema.STRING_SCHEMA)
            .build();

        Struct inputValue = new Struct(inputSchema)
            .put("payload", b64);

        SourceRecord record = new SourceRecord(
            null, null,
            "orders.json", 0,
            null, null,
            inputSchema, inputValue
        );

        SourceRecord out = xform.apply(record);

        assertEquals("abc-123", out.headers().lastWithName("correlationId").value());
        assertEquals("crm", out.headers().lastWithName("source").value());
        assertEquals("ORDER_CREATED", out.headers().lastWithName("eventType").value());

        Struct outValue = (Struct) out.value();
        assertEquals(101, outValue.getInt32("orderId"));
    }

    @Test
    void shouldExtractJsonAsStructForMappedJsonTopic() {
        ExtractDecodedPayload.Value<SourceRecord> xform =
            new ExtractDecodedPayload.Value<>();

        xform.configure(Map.of(
            "field", "payload",
            "topic.format.mapping", "orders.json:json,orders.xml:xml",
            "default.format", "json",
            "fail.on.invalid", "true"
        ));

        String decodedJson = """
            {
              "orderId": 101,
              "status": "NEW",
              "amount": 12.5,
              "customer": {
                "name": "Acme",
                "tier": "GOLD"
              }
            }
            """;

        String b64 = Base64.getEncoder()
            .encodeToString(decodedJson.getBytes(StandardCharsets.UTF_8));

        Schema inputSchema = SchemaBuilder.struct()
            .field("id", Schema.STRING_SCHEMA)
            .field("payload", Schema.STRING_SCHEMA)
            .build();

        Struct inputValue = new Struct(inputSchema)
            .put("id", "123")
            .put("payload", b64);

        SourceRecord record = new SourceRecord(
            null, null,
            "orders.json", 0,
            null, null,
            inputSchema, inputValue
        );

        SourceRecord out = xform.apply(record);

        assertNotNull(out.valueSchema());
        assertEquals(Schema.Type.STRUCT, out.valueSchema().type());

        Struct outValue = (Struct) out.value();
        assertEquals(101, outValue.getInt32("orderId"));
        assertEquals("NEW", outValue.getString("status"));
        assertEquals(12.5d, outValue.getFloat64("amount"));

        Struct customer = outValue.getStruct("customer");
        assertEquals("Acme", customer.getString("name"));
        assertEquals("GOLD", customer.getString("tier"));
    }

    @Test
    void shouldExtractXmlAsStringForMappedXmlTopic() {
        ExtractDecodedPayload.Value<SourceRecord> xform =
            new ExtractDecodedPayload.Value<>();

        xform.configure(Map.of(
            "field", "payload",
            "topic.format.mapping", "orders.json:json,orders.xml:xml",
            "default.format", "json",
            "fail.on.invalid", "true"
        ));

        String decodedXml = "<root><status>NEW</status></root>";
        String b64 = Base64.getEncoder()
            .encodeToString(decodedXml.getBytes(StandardCharsets.UTF_8));

        Schema inputSchema = SchemaBuilder.struct()
            .field("payload", Schema.STRING_SCHEMA)
            .build();

        Struct inputValue = new Struct(inputSchema)
            .put("payload", b64);

        SourceRecord record = new SourceRecord(
            null, null,
            "orders.xml", 0,
            null, null,
            inputSchema, inputValue
        );

        SourceRecord out = xform.apply(record);

        assertEquals(Schema.Type.STRING, out.valueSchema().type());
        assertEquals(decodedXml, out.value());
    }

    @Test
    void shouldUseDefaultFormatWhenTopicNotMapped() {
        ExtractDecodedPayload.Value<SourceRecord> xform =
            new ExtractDecodedPayload.Value<>();

        xform.configure(Map.of(
            "field", "payload",
            "topic.format.mapping", "orders.xml:xml",
            "default.format", "json",
            "fail.on.invalid", "true"
        ));

        String decodedJson = "{\"k\":1,\"v\":\"x\"}";
        String b64 = Base64.getEncoder()
            .encodeToString(decodedJson.getBytes(StandardCharsets.UTF_8));

        Schema inputSchema = SchemaBuilder.struct()
            .field("payload", Schema.STRING_SCHEMA)
            .build();

        Struct inputValue = new Struct(inputSchema)
            .put("payload", b64);

        SourceRecord record = new SourceRecord(
            null, null,
            "unmapped.topic", 0,
            null, null,
            inputSchema, inputValue
        );

        SourceRecord out = xform.apply(record);

        assertEquals(Schema.Type.STRUCT, out.valueSchema().type());
        Struct outValue = (Struct) out.value();
        assertEquals(1, outValue.getInt32("k"));
        assertEquals("x", outValue.getString("v"));
    }

    @Test
    void shouldLeaveRecordUnchangedWhenInvalidBase64AndFailFalse() {
        ExtractDecodedPayload.Value<SourceRecord> xform =
            new ExtractDecodedPayload.Value<>();

        xform.configure(Map.of(
            "field", "payload",
            "topic.format.mapping", "orders.json:json,orders.xml:xml",
            "default.format", "json",
            "fail.on.invalid", "false"
        ));

        Schema inputSchema = SchemaBuilder.struct()
            .field("payload", Schema.STRING_SCHEMA)
            .build();

        Struct inputValue = new Struct(inputSchema)
            .put("payload", "not-base64");

        SourceRecord record = new SourceRecord(
            null, null,
            "orders.json", 0,
            null, null,
            inputSchema, inputValue
        );

        SourceRecord out = xform.apply(record);

        assertEquals(record.valueSchema(), out.valueSchema());
        assertInstanceOf(Struct.class, out.value());
        assertEquals("not-base64", ((Struct) out.value()).getString("payload"));
    }

    @Test
    void shouldLeaveRecordUnchangedWhenInvalidJsonAndFailFalse() {
        ExtractDecodedPayload.Value<SourceRecord> xform =
            new ExtractDecodedPayload.Value<>();

        String notJson = "<root>not-json</root>";
        String b64 = Base64.getEncoder()
            .encodeToString(notJson.getBytes(StandardCharsets.UTF_8));

        xform.configure(Map.of(
            "field", "payload",
            "topic.format.mapping", "orders.json:json",
            "default.format", "json",
            "fail.on.invalid", "false"
        ));

        Schema inputSchema = SchemaBuilder.struct()
            .field("payload", Schema.STRING_SCHEMA)
            .build();

        Struct inputValue = new Struct(inputSchema)
            .put("payload", b64);

        SourceRecord record = new SourceRecord(
            null, null,
            "orders.json", 0,
            null, null,
            inputSchema, inputValue
        );

        SourceRecord out = xform.apply(record);

        assertEquals(record.valueSchema(), out.valueSchema());
        assertEquals(record.value(), out.value());
    }
}
