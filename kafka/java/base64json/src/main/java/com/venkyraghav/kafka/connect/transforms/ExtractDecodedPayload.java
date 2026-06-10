package com.venkyraghav.kafka.connect.transforms;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigException;
import org.apache.kafka.connect.connector.ConnectRecord;
import org.apache.kafka.connect.data.Field;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.errors.DataException;
import org.apache.kafka.connect.transforms.Transformation;
import org.apache.kafka.connect.transforms.util.SimpleConfig;

import java.util.Collections;
import org.apache.kafka.connect.header.ConnectHeaders;
import org.apache.kafka.connect.header.Headers;

public abstract class ExtractDecodedPayload<R extends ConnectRecord<R>> implements Transformation<R> {

    public static final String FIELD_CONFIG = "field";
    public static final String TOPIC_FORMAT_MAPPING_CONFIG = "topic.format.mapping";
    public static final String DEFAULT_FORMAT_CONFIG = "default.format";
    public static final String FAIL_ON_INVALID_CONFIG = "fail.on.invalid";
    public static final String JSON_HEADER_FIELDS_CONFIG = "json.header.fields";
    public static final String JSON_HEADER_NAMES_CONFIG = "json.header.names";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static final ConfigDef CONFIG_DEF = new ConfigDef()
        .define(FIELD_CONFIG, ConfigDef.Type.STRING, ConfigDef.Importance.HIGH, "Top-level field to decode")
        .define(TOPIC_FORMAT_MAPPING_CONFIG, ConfigDef.Type.STRING, "", ConfigDef.Importance.HIGH,
            "Comma-separated topic:format mappings, e.g. orders.json:json,orders.xml:xml")
        .define(DEFAULT_FORMAT_CONFIG, ConfigDef.Type.STRING, "json", ConfigDef.Importance.MEDIUM,
            "Default decoded payload format when topic is not mapped. Supported: json, xml")
        .define(FAIL_ON_INVALID_CONFIG, ConfigDef.Type.BOOLEAN, true, ConfigDef.Importance.MEDIUM,
            "If true, fail on invalid Base64 / payload. If false, leave the record unchanged.")
        .define(JSON_HEADER_FIELDS_CONFIG, ConfigDef.Type.LIST, Collections.emptyList(), ConfigDef.Importance.MEDIUM,
            "JSON field paths to copy into record headers for JSON topics. Supports dotted paths, e.g. metadata.correlationId")
        .define(JSON_HEADER_NAMES_CONFIG, ConfigDef.Type.LIST, Collections.emptyList(), ConfigDef.Importance.MEDIUM,
            "Header names corresponding to json.header.fields. If empty, field paths are used as header names");

    private String fieldName;
    private String defaultFormat;
    private boolean failOnInvalid;
    private Map<String, String> topicFormatMapping;
    private List<String> jsonHeaderFields;
    private List<String> jsonHeaderNames;

    @Override
    public void configure(Map<String, ?> configs) {
        SimpleConfig config = new SimpleConfig(CONFIG_DEF, configs);
        this.fieldName = config.getString(FIELD_CONFIG);
        this.defaultFormat = config.getString(DEFAULT_FORMAT_CONFIG).trim().toLowerCase();
        this.failOnInvalid = config.getBoolean(FAIL_ON_INVALID_CONFIG);
        this.topicFormatMapping = parseTopicFormatMapping(config.getString(TOPIC_FORMAT_MAPPING_CONFIG));
        this.jsonHeaderFields = config.getList(JSON_HEADER_FIELDS_CONFIG);
        this.jsonHeaderNames = config.getList(JSON_HEADER_NAMES_CONFIG);

        validateFormat(defaultFormat, DEFAULT_FORMAT_CONFIG);
        for (Map.Entry<String, String> e : topicFormatMapping.entrySet()) {
            validateFormat(e.getValue(), TOPIC_FORMAT_MAPPING_CONFIG + " for topic " + e.getKey());
        }

        if (!jsonHeaderNames.isEmpty() && jsonHeaderNames.size() != jsonHeaderFields.size()) {
            throw new ConfigException(
                JSON_HEADER_NAMES_CONFIG,
                jsonHeaderNames,
                "json.header.names must be empty or have the same number of entries as json.header.fields"
            );
        }
    }

    @Override
    public R apply(R record) {
        Object container = operatingValue(record);
        if (container == null) {
            return record;
        }

        try {
            Object encodedFieldValue = extractField(container, fieldName);
            if (encodedFieldValue == null) {
                return record;
            }

            String encoded;
            if (encodedFieldValue instanceof String) {
                encoded = (String) encodedFieldValue;
            } else if (encodedFieldValue instanceof byte[]) {
                encoded = new String((byte[]) encodedFieldValue, StandardCharsets.UTF_8);
            } else {
                throw new DataException("Field '" + fieldName + "' must be String or byte[]");
            }

            String decoded = new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);
            String format = topicFormatMapping.getOrDefault(record.topic(), defaultFormat);

            switch (format) {
                case "xml": {
                    Headers headers = new ConnectHeaders(record.headers());
                    return newRecord(record, Schema.OPTIONAL_STRING_SCHEMA, decoded, headers);
                }

                case "json": {
                    JsonNode root = MAPPER.readTree(decoded);
                    if (root == null || root.isNull() || !root.isObject()) {
                        throw new DataException("Decoded JSON for topic '" + record.topic() + "' must be a JSON object");
                    }

                    Headers headers = new ConnectHeaders(record.headers());
                    addJsonHeaders(root, headers);

                    Schema newSchema = inferSchema(root, "decoded_" + sanitize(record.topic()) + "_" + fieldName);
                    Struct newValue = toStruct(root, newSchema);
                    return newRecord(record, newSchema, newValue, headers);
                }

                default:
                    throw new DataException("Unsupported format '" + format + "' for topic '" + record.topic() + "'");
            }
        } catch (Exception e) {
            if (failOnInvalid) {
                throw new DataException("Failed to decode field '" + fieldName + "' for topic '" + record.topic() + "'", e);
            }
            return record;
        }
    }

    private void addJsonHeaders(JsonNode root, Headers headers) {
        for (int i = 0; i < jsonHeaderFields.size(); i++) {
            String fieldPath = jsonHeaderFields.get(i);
            String headerName = jsonHeaderNames.isEmpty() ? fieldPath : jsonHeaderNames.get(i);

            JsonNode node = getNodeByPath(root, fieldPath);
            if (node == null || node.isMissingNode() || node.isNull()) {
                continue;
            }

            Schema schema = headerSchema(node);
            Object value = headerValue(node);

            headers.add(headerName, value, schema);
        }
    }

    private JsonNode getNodeByPath(JsonNode root, String path) {
        JsonNode current = root;
        for (String part : path.split("\\.")) {
            if (current == null || current.isMissingNode() || !current.isObject()) {
                return null;
            }
            current = current.get(part);
        }
        return current;
    }

    private Schema headerSchema(JsonNode node) {
        if (node.isTextual()) return Schema.OPTIONAL_STRING_SCHEMA;
        if (node.isBoolean()) return Schema.OPTIONAL_BOOLEAN_SCHEMA;
        if (node.isInt()) return Schema.OPTIONAL_INT32_SCHEMA;
        if (node.isLong()) return Schema.OPTIONAL_INT64_SCHEMA;
        if (node.isFloat() || node.isDouble() || node.isBigDecimal()) return Schema.OPTIONAL_FLOAT64_SCHEMA;

        // objects / arrays / everything else -> stringified JSON
        return Schema.OPTIONAL_STRING_SCHEMA;
    }

    private Object headerValue(JsonNode node) {
        if (node.isTextual()) return node.asText();
        if (node.isBoolean()) return node.asBoolean();
        if (node.isInt()) return node.asInt();
        if (node.isLong()) return node.asLong();
        if (node.isFloat() || node.isDouble() || node.isBigDecimal()) return node.asDouble();

        // objects / arrays / everything else -> stringified JSON
        return node.toString();
    }

    private Map<String, String> parseTopicFormatMapping(String raw) {
        Map<String, String> result = new HashMap<>();
        if (raw == null || raw.trim().isEmpty()) {
            return result;
        }

        for (String pair : raw.split(",")) {
            String trimmed = pair.trim();
            if (trimmed.isEmpty()) {
                continue;
            }

            String[] parts = trimmed.split(":");
            if (parts.length != 2) {
                throw new ConfigException(TOPIC_FORMAT_MAPPING_CONFIG, raw,
                    "Invalid mapping format. Use 'topic:format' (e.g. 'orders.json:json')");
            }

            String topic = parts[0].trim();
            String format = parts[1].trim().toLowerCase();

            if (topic.isEmpty() || format.isEmpty()) {
                throw new ConfigException(TOPIC_FORMAT_MAPPING_CONFIG, raw,
                    "Topic and format must both be non-empty");
            }

            if (result.containsKey(topic)) {
                throw new ConfigException(TOPIC_FORMAT_MAPPING_CONFIG, raw,
                    "Topic '" + topic + "' mapped multiple times");
            }

            result.put(topic, format);
        }

        return result;
    }

    private void validateFormat(String format, String configName) {
        if (!"json".equals(format) && !"xml".equals(format)) {
            throw new ConfigException(configName, format, "Supported formats are: json, xml");
        }
    }

    private Object extractField(Object container, String field) {
        if (container instanceof Struct struct) {
            Field f = struct.schema().field(field);
            return f == null ? null : struct.get(field);
        }
        if (container instanceof Map<?, ?> map) {
            return map.get(field);
        }
        throw new DataException("Expected Struct or Map record value, found: " + container.getClass().getName());
    }

    private Schema inferSchema(JsonNode node, String name) {
        if (node.isObject()) {
            SchemaBuilder builder = SchemaBuilder.struct().name(name);
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> e = fields.next();
                builder.field(e.getKey(), inferFieldSchema(e.getValue(), name + "." + e.getKey()));
            }
            return builder.build();
        }

        if (node.isArray()) {
            Schema elementSchema = inferArrayElementSchema(node, name + ".item");
            return SchemaBuilder.array(elementSchema).optional().build();
        }

        if (node.isTextual()) return Schema.OPTIONAL_STRING_SCHEMA;
        if (node.isBoolean()) return Schema.OPTIONAL_BOOLEAN_SCHEMA;
        if (node.isInt()) return Schema.OPTIONAL_INT32_SCHEMA;
        if (node.isLong()) return Schema.OPTIONAL_INT64_SCHEMA;
        if (node.isFloat() || node.isDouble() || node.isBigDecimal()) return Schema.OPTIONAL_FLOAT64_SCHEMA;
        if (node.isNull()) return Schema.OPTIONAL_STRING_SCHEMA;

        throw new DataException("Unsupported JSON node type: " + node.getNodeType());
    }

    private Schema inferFieldSchema(JsonNode node, String name) {
        Schema schema = inferSchema(node, name);
        if (!schema.isOptional()) {
            switch (schema.type()) {
                case STRUCT:
                    return copyStructOptional(schema);
                case ARRAY:
                    return SchemaBuilder.array(schema.valueSchema()).optional().build();
                case STRING:
                    return Schema.OPTIONAL_STRING_SCHEMA;
                case BOOLEAN:
                    return Schema.OPTIONAL_BOOLEAN_SCHEMA;
                case INT32:
                    return Schema.OPTIONAL_INT32_SCHEMA;
                case INT64:
                    return Schema.OPTIONAL_INT64_SCHEMA;
                case FLOAT64:
                    return Schema.OPTIONAL_FLOAT64_SCHEMA;
                default:
                    return schema;
            }
        }
        return schema;
    }

    private Schema inferArrayElementSchema(JsonNode arrayNode, String name) {
        for (JsonNode item : arrayNode) {
            if (item != null && !item.isNull()) {
                return inferFieldSchema(item, name);
            }
        }
        return Schema.OPTIONAL_STRING_SCHEMA;
    }

    private Schema copyStructOptional(Schema schema) {
        SchemaBuilder builder = SchemaBuilder.struct().name(schema.name()).optional();
        for (Field field : schema.fields()) {
            builder.field(field.name(), field.schema());
        }
        return builder.build();
    }

    private Struct toStruct(JsonNode node, Schema schema) {
        Struct struct = new Struct(schema);
        for (Field field : schema.fields()) {
            JsonNode child = node.get(field.name());
            struct.put(field.name(), toConnectValue(child, field.schema()));
        }
        return struct;
    }

    private Object toConnectValue(JsonNode node, Schema schema) {
        if (node == null || node.isNull()) {
            return null;
        }

        switch (schema.type()) {
            case STRUCT:
                return toStruct(node, schema);
            case ARRAY:
                List<Object> list = new ArrayList<>();
                for (JsonNode item : node) {
                    list.add(toConnectValue(item, schema.valueSchema()));
                }
                return list;
            case STRING:
                return node.asText();
            case BOOLEAN:
                return node.asBoolean();
            case INT32:
                return node.asInt();
            case INT64:
                return node.asLong();
            case FLOAT64:
                return node.asDouble();
            default:
                throw new DataException("Unsupported schema type: " + schema.type());
        }
    }

    private String sanitize(String value) {
        return value.replaceAll("[^A-Za-z0-9_]", "_");
    }

    @Override
    public ConfigDef config() {
        return CONFIG_DEF;
    }

    @Override
    public void close() {
    }

    protected abstract Object operatingValue(R record);

    protected abstract R newRecord(R record, Schema updatedSchema, Object updatedValue, Headers headers);

    public static class Value<R extends ConnectRecord<R>> extends ExtractDecodedPayload<R> {
        @Override
        protected Object operatingValue(R record) {
            return record.value();
        }

        @Override
        protected R newRecord(R record, Schema updatedSchema, Object updatedValue, Headers headers) {
            return record.newRecord(
                record.topic(),
                record.kafkaPartition(),
                record.keySchema(),
                record.key(),
                updatedSchema,
                updatedValue,
                record.timestamp(),
                headers
            );
        }
    }
}
