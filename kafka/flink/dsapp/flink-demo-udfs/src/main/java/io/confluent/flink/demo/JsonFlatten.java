package io.confluent.flink.demo;

import java.util.Iterator;
import java.util.Map;

import org.apache.flink.table.functions.ScalarFunction;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonFlatten extends ScalarFunction {

    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Flattens the given JSON string using the default separator (".").
     *
     * @param json the JSON string to flatten
     * @return the flattened JSON string
     */
    public String eval(String json) {
        return eval(json, ".");
    }

    /**
     * Flattens the given JSON string with provided separator.
     *
     * @param json the JSON string to flatten
     * @param delimiter te delimiter to use as separator
     * 
     * @return the flattened JSON string
     */
    public String eval(String json, String delimiter) {
        try {
            JsonNode root = mapper.readTree(json);
            StringBuffer flattenedJson = new StringBuffer();
            flatten("", root, flattenedJson, delimiter);
            return "{" + flattenedJson.toString() + "}";
        } catch (JsonProcessingException e) {
            System.err.println("Error Parsing in Eval " + e);
            return ""; // Or throw
        }
    }

    private void flatten (String prefix, JsonNode node, StringBuffer flattenedJson, String delimiter) {
        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                
                String key = prefix.isEmpty()
                    ? field.getKey()
                    : prefix + delimiter + field.getKey();
                flatten(key, field.getValue(), flattenedJson, delimiter);
                if (fields.hasNext()) {
                    flattenedJson.append(",");
                }
            }
        } else if (node.isValueNode()) {
            flattenedJson.append("\"").append(prefix).append("\":");
            
            // Without schema definition node always evaluates as Textual
            if (node.isTextual() || node.isEmpty()) {
                flattenedJson.append("\"").append(node.asText("")).append("\"");
            } else if (node.isBinary() || node.isBoolean()) {
                flattenedJson.append(node.asBoolean(false));
            } else if (node.isDouble() || node.isFloat() || node.isFloatingPointNumber() || node.isBigDecimal()) {
                flattenedJson.append(node.asDouble(0.0));
            } else if (node.isBigInteger() || node.isLong()) {
                flattenedJson.append(node.asLong(0L));
            } else if (node.isInt() || node.isIntegralNumber() || node.isNumber() || node.isShort()) {
                flattenedJson.append(node.asInt(0));
            }
        }
    }
}
