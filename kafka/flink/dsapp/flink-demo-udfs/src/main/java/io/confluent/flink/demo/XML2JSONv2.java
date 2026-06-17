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

import javax.xml.stream.XMLInputFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.flink.table.functions.FunctionContext;
import org.apache.flink.table.functions.ScalarFunction;
import org.apache.flink.util.StringUtils;

import com.ctc.wstx.stax.WstxInputFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

class LookupResource {
    static Map<String, String> resources = new HashMap<String, String>();
    static Map<String, String> errors = new HashMap<String, String>();

    static void clear() {
        resources.clear();
        errors.clear();
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

    static String putError(String resourceName, String resourceContent) {
        if (StringUtils.isNullOrWhitespaceOnly(resourceName) || StringUtils.isNullOrWhitespaceOnly(resourceContent)) {
            return null;
        }
        return errors.put(resourceName, resourceContent);
    }

    static String getError(String resourceName) {
        if (StringUtils.isNullOrWhitespaceOnly(resourceName)) {
            return null;
        }
        return errors.get(resourceName);
    }

    static boolean containsError(String resourceName) {
        if (StringUtils.isNullOrWhitespaceOnly(resourceName)) {
            return false;
        }
        return errors.containsKey(resourceName);
    }

    static String getJson(Map<String, String> map) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            return "{\"Exception\" : \"JsonProcessingException\", \"Message\" : \"" + e.getMessage() + " converting map to json\"}";
        }
    }
}

public class XML2JSONv2 extends ScalarFunction {
    private transient String endpoint;
    private transient long REFRESH_TIME;
    private transient long loadTime;
    private transient Map<String, String> errMap;
    private transient HttpClient httpClient;

    public XML2JSONv2(){
        this.errMap = new HashMap<String, String>();
        httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
    }

    public XML2JSONv2(HttpClient httpClient){
        this.errMap = new HashMap<String, String>();
        this.httpClient = httpClient;
    }

    @Override
    public void open(FunctionContext context) throws Exception {
        // Retrieve connection details using the connection name defined in SQL
        if (context != null) {
            this.endpoint = context.getJobParameter("GITHUB_RAWCONTENT.endpoint", null);
        } else {
            errMap.put("context", "is null");
            //System.out.println("context is null");
        }
        REFRESH_TIME = Duration.ofHours(4).toMillis();
        loadTime = 0L;

        if (StringUtils.isNullOrWhitespaceOnly(this.endpoint)) {
            errMap.put("endpoint", "is not defined");
        }
        // System.out.println("Endpoint => " + this.endpoint);
    }

    public String eval(String xml, String xmlSchemaName) {
        if (xml == null) {
            return "";
        }
        return eval(xml.getBytes(), xmlSchemaName);
    }

    public String eval(byte[] xml, String xmlSchemaName) {
        if (xml == null || xml.length == 0) {
            return "";
        }

        try {
            String xsd = "";
            if (StringUtils.isNullOrWhitespaceOnly(xmlSchemaName) == false) {
                xsd = retrieveResource(xmlSchemaName);
                if (xsd == null) {
                    if (LookupResource.containsError(xmlSchemaName)) {
                        errMap.put("Error", "schema is " + xmlSchemaName + ". refer other errors");
                    } else {
                        errMap.put("Error", "schema" + xmlSchemaName + " not found");
                    }
                    return LookupResource.getJson(errMap);
                }
            }
            return convert(xml, xsd);
        } catch (Exception e) {
            e.printStackTrace();
            errMap.put("Exception", e.getClass().getName());
            errMap.put("Message", e.getMessage());
            return LookupResource.getJson(errMap);
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

            XMLInputFactory inputFactory = new WstxInputFactory();
            // Disable namespace processing so prefixes and xsi elements are treated as plain text or skipped
            inputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.FALSE);

            // Step 1: Read XML into JsonNode
            XmlMapper xmlMapper = new XmlMapper(new XmlFactory(inputFactory, null));

            //xmlMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            JsonNode node = xmlMapper.readTree(xml);

            // Step 2: Convert JsonNode to JSON string
            ObjectMapper jsonMapper = new ObjectMapper();
            return jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(node);
        } catch (Exception e) {
            e.printStackTrace();
            errMap.put("Exception", e.getClass().getName());
            errMap.put("Message", e.getMessage());
            return LookupResource.getJson(errMap);
        }
    }

    private void validate(byte[] xml, String xsd) throws Exception {
        SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");

        Schema schema = factory.newSchema(new StreamSource(new StringReader(xsd), "dummy.xsd"));
        Validator validator = schema.newValidator();

        validator.validate(new StreamSource(new ByteArrayInputStream(xml), "dummy.xml"));
    }

    private String retrieveResource(String resourceName) throws IOException, InterruptedException {
        if (System.currentTimeMillis() - loadTime > REFRESH_TIME) {
            loadTime = System.currentTimeMillis();
            LookupResource.clear();
        }
        if (LookupResource.containsError(resourceName)) {
            return null;
        }
        if (LookupResource.containsKey(resourceName)) {
            return LookupResource.get(resourceName);
        }
        try {
            // System.out.println("URI => " + this.endpoint + "venkyraghav/restartgo/refs/heads/main/" + resourceName);
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
                default -> {
                    errMap.put("statusCode", String.valueOf(response.statusCode()));
                    errMap.put("url", this.endpoint + "venkyraghav/restartgo/refs/heads/main/" + resourceName);
                    LookupResource.putError(resourceName, LookupResource.getJson(errMap));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            errMap.put("Exception", e.getClass().getName());
            errMap.put("Message", e.getMessage());
            LookupResource.putError(resourceName, LookupResource.getJson(errMap));
        }
        return null;
    }
}
