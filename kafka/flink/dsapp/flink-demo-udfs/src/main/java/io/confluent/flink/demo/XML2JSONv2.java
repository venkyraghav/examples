package io.confluent.flink.demo;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.flink.table.functions.FunctionContext;
import org.apache.flink.table.functions.ScalarFunction;
import org.apache.flink.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

class LookupResource {
    static Map<String, String> resources = new HashMap<String, String>();

    static void clear() {
        resources.clear();
    }

    static String put(String resourceName, String resourceContent) {
        if (StringUtils.isNullOrWhitespaceOnly(resourceName) || StringUtils.isNullOrWhitespaceOnly(resourceContent)) {
            return null;
        }
        return resources.put(resourceName, resourceContent);
    }

    static String get(String resourceName) {
        if (StringUtils.isNullOrWhitespaceOnly(resourceName)) {
            return null;
        }
        return resources.get(resourceName);
    }

    static boolean containsKey(String resourceName) {
        if (StringUtils.isNullOrWhitespaceOnly(resourceName)) {
            return false;
        }
        return resources.containsKey(resourceName);
    }
}

public class XML2JSONv2 extends ScalarFunction {
    private String endpoint;

    private final static long REFRESH_TIME = Duration.ofHours(4).toMillis();
    private final static String XML2JSON2_NAME = "xml2json2_name";
    private final static String XML2JSON2_ENDPOINT = "https://raw.githubusercontent.com/venkyraghav/restartgo/refs/heads/main";
    private final static String XML2JSON2_CONNECTION = """
        CREATE CONNECTION GITHUB_RAWCONTENT
        WITH ('type' = 'REST', 'endpoint' = 'https://raw.githubusercontent.com/', 'token' = '');
    """;

    private transient long loadTime = 0L;
    // private transient Map<String, String> resources = new HashMap<String, String>();

    @Override
    public void open(FunctionContext context) throws Exception {
        // Retrieve connection details using the connection name defined in SQL
        if (context != null) {
            this.endpoint = context.getJobParameter("GITHUB_RAWCONTENT", null);
        }

        if (StringUtils.isNullOrWhitespaceOnly(this.endpoint)) {
            this.endpoint = XML2JSON2_ENDPOINT;
        }
    }

    public String eval(String xml, String xmlSchemaName) {
        if (xml == null) {
            System.out.println("xml is empty");
            return "";
        }
        return eval(xml.getBytes(), xmlSchemaName);
    }

    public String eval(byte[] xml, String xmlSchemaName) {
        if (xml == null || xml.length == 0) {
            System.out.println("xml is empty");
            return "";
        }

        try {
            String xsd = "";
            if (StringUtils.isNullOrWhitespaceOnly(xmlSchemaName) == false) {
                xsd = retrieveResource(xmlSchemaName);
                if (xsd == null) {
                    System.out.println("xsd not found for " + xmlSchemaName);
                    return "";
                }
            }
            return convert(xml, xsd);
        } catch (Exception e) {
            System.out.println("Exception " + e);

            return "{\"Exception\": \"" +  e.getLocalizedMessage() + "\"}";
        }
    }

    private String convert(byte[] xml, String xmlSchema) throws Exception {
        if (xml == null || xml.length == 0) {
            return "";
        }
        try {
            if (StringUtils.isNullOrWhitespaceOnly(String.valueOf(xml))) {
                return "";
            }
            if (StringUtils.isNullOrWhitespaceOnly(xmlSchema) == false) {
                validate(xml, xmlSchema);
            }

            // Step 1: Read XML into JsonNode
            XmlMapper xmlMapper = new XmlMapper();
            JsonNode node = xmlMapper.readTree(xml);

            // Step 2: Convert JsonNode to JSON string
            ObjectMapper jsonMapper = new ObjectMapper();
            return jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(node);
        } catch (IOException e) {
            System.out.println("Exception " + e);
            return "{\"Exception\": \"" +  e.getLocalizedMessage() + "\"}";
        }
    }

    private void validate(byte[] xml, String xsd) throws Exception {
        // log.info("XSD {}", xsd);
        // System.out.println("XSD " + xsd);
        SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");

        Schema schema = factory.newSchema(new StreamSource(new StringReader(xsd)));
        Validator validator = schema.newValidator();

        validator.validate(new StreamSource(new ByteArrayInputStream(xml)));
    }

    private String retrieveResource(String resourceName) throws IOException, InterruptedException {
        if (System.currentTimeMillis() - loadTime > REFRESH_TIME) {
            loadTime = System.currentTimeMillis();
            LookupResource.clear();
        }
        if (LookupResource.containsKey(resourceName)) {
            return LookupResource.get(resourceName);
        }
        // log.info("Endpoint is {}", this.endpoint + "/" + resourceName);
        // System.out.println("Endpoint is " + this.endpoint + "/" + resourceName);
        // Initialize HttpClient
        HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

        System.out.println("HTTP URL=> " + this.endpoint + "venkyraghav/restartgo/refs/heads/main/" + resourceName);
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(this.endpoint + "venkyraghav/restartgo/refs/heads/main/" + resourceName))
            .GET()
            .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        switch (response.statusCode()) {
            case 200 -> {
                LookupResource.put(resourceName, response.body());
                if (loadTime == 0) {
                    loadTime = System.currentTimeMillis();
                } else if (System.currentTimeMillis() - loadTime > REFRESH_TIME) {
                    loadTime = System.currentTimeMillis();
                    LookupResource.clear();
                }
                return LookupResource.get(resourceName);
            }
            case 404 -> { // TODO Build a cache of failed lookups

            }
        }
        return null;
    }
}
