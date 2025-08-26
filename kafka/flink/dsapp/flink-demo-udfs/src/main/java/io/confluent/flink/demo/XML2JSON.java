package io.confluent.flink.demo;
import java.io.IOException;

import org.apache.flink.table.functions.ScalarFunction;
import org.apache.flink.util.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

public class XML2JSON extends ScalarFunction {

    public String eval(byte[] xml) {
        if (xml == null)
            return eval("");
        return eval(new String(xml));
    }
    
    public String eval(String xml) {
        try {
            if (StringUtils.isNullOrWhitespaceOnly(xml))
                return "{}";
            // Step 1: Read XML into JsonNode
            XmlMapper xmlMapper = new XmlMapper();
            JsonNode node = xmlMapper.readTree(xml.getBytes());

            // Step 2: Convert JsonNode to JSON string
            ObjectMapper jsonMapper = new ObjectMapper();
            return jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(node);
        } catch (IOException e) {
            System.out.println(e.toString());
            return "{\"Exception\": \"" +  e.getLocalizedMessage() + "\"}";
        }
    }

}


