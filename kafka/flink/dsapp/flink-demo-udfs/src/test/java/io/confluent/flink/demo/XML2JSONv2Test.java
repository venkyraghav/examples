package io.confluent.flink.demo;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import static org.junit.jupiter.api.Assertions.fail;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpHeaders;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest;
import java.net.http.HttpClient;
import java.util.Optional;
import java.io.IOException;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.apache.flink.table.functions.FunctionContext;

@ExtendWith(MockitoExtension.class)
public class XML2JSONv2Test {
    @Mock
    private HttpClient httpClient;

    @Mock
    private HttpResponse httpResponse;

    @Mock
    private HttpRequest httpRequest;

    @Mock
    private FunctionContext context;

    private final String response404 = "";
    private final String response200 = """
                {"id":"112","name":"Cookware Set","price":"120.00","details":{"category":"Kitchen","quantity":"4"}}
                """;

    private void assertJson(String expectedJson, String actualJson) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode expected = mapper.readTree(expectedJson);
            JsonNode actual = mapper.readTree(actualJson);
            assertThat(actual).isEqualTo(expected);
        } catch (JsonProcessingException ex) {
            ex.printStackTrace();
            assertThat(false);
        }
    }

    private XML2JSONv2 init() {
        XML2JSONv2 ret = new XML2JSONv2();
        try {
            // when(context.getJobParameter("GITHUB_RAWCONTENT", "default")).thenReturn("actual-value");
            ret.open(context);
        }
        catch (Exception e) {}
        return ret;
    }

    @Test
    public void testXmlStringNull() {
        String input = null;
        String expected = "";

        String got = init().eval(input, "test");

        assertJson(expected, got);
    }

    @Test
    public void testXmlStringEmpty() {
        String input = "";
        String expected = "";
        String got = init().eval(input, "test");

        assertJson(expected, got);
    }

    @Test
    public void testXmlByteNull() {
        byte[] input = null;
        String expected = "";
        String got = init().eval(input, "test");

        assertJson(expected, got);
    }

    @Test
    public void testXmlSchemaNameNull() {
        String input = """
    <record>
        <id>112</id>
        <name>Cookware Set</name>
        <price>120.00</price>
        <details>
            <category>Kitchen</category>
            <quantity>4</quantity>
        </details>
    </record>
                """;
        String expected = """
                {"id":"112","name":"Cookware Set","price":"120.00","details":{"category":"Kitchen","quantity":"4"}}
                """;
        try {
            String got = init().eval(input, null);

            assertJson(expected, got);
        } catch (Exception e) {
            fail("exception thrown " + e.getLocalizedMessage());
        }
    }

    @Test
    public void testXmlSchemaEmpty() {
        String input = """
    <record>
        <id>112</id>
        <name>Cookware Set</name>
        <price>120.00</price>
        <details>
            <category>Kitchen</category>
            <quantity>4</quantity>
        </details>
    </record>
                """;
        String expected = """
                {"id":"112","name":"Cookware Set","price":"120.00","details":{"category":"Kitchen","quantity":"4"}}
                """;
        try {
            String got = init().eval(input, "");

            assertJson(expected, got);
        } catch (Exception e) {
            fail("exception thrown " + e.getLocalizedMessage());
        }
    }

    @Test
    public void testXmlSchemaNameGood() throws IOException, InterruptedException {
        String input = """
    <record>
        <id>112</id>
        <name>Cookware Set</name>
        <price>120.00</price>
        <details>
            <category>Kitchen</category>
            <quantity>4</quantity>
        </details>
    </record>
                """;
        String expected = """
                {"id":"112","name":"Cookware Set","price":"120.00","details":{"category":"Kitchen","quantity":"4"}}
                """;

        when(context.getJobParameter("GITHUB_RAWCONTENT", null)).thenReturn("https://raw.githubusercontent1.com/");
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(expected);
        when(httpClient.send(any(HttpRequest.class), any())).thenReturn(httpResponse);

        String got = init().eval(input, "inventory.xsd");

        assertJson(expected, got);
    }

    @Test
    public void testXmlBytesSchemaNameGood() {
        String input = """
    <record>
        <id>112</id>
        <name>Cookware Set</name>
        <price>120.00</price>
        <details>
            <category>Kitchen</category>
            <quantity>4</quantity>
        </details>
    </record>
                """;
        String expected = """
                {"id":"112","name":"Cookware Set","price":"120.00","details":{"category":"Kitchen","quantity":"4"}}
                """;
        try {
            when(context.getJobParameter("GITHUB_RAWCONTENT", null)).thenReturn("https://raw.githubusercontent1.com/");
            when(httpResponse.statusCode()).thenReturn(200);
            when(httpResponse.body()).thenReturn(expected);
            when(httpClient.send(any(HttpRequest.class), any())).thenReturn(httpResponse);

            String got = init().eval(input.getBytes(), "inventory.xsd");

            assertJson(expected, got);
        } catch (Exception e) {
            fail("exception thrown " + e.getLocalizedMessage());
        }

    }

    @Test
    public void testXmlSchemaNameNotFound() {
        String input = """
    <record>
        <id>112</id>
        <name>Cookware Set</name>
        <price>120.00</price>
        <details>
            <category>Kitchen</category>
            <quantity>4</quantity>
        </details>
    </record>
                """;
        String expected = "";

        try {
            when(context.getJobParameter("GITHUB_RAWCONTENT", null)).thenReturn("https://raw.githubusercontent1.com/");
            when(httpResponse.statusCode()).thenReturn(404);
            when(httpResponse.body()).thenReturn(expected);
            when(httpClient.send(any(HttpRequest.class), any())).thenReturn(httpResponse);

            String got = init().eval(input.getBytes(), "inventory2.xsd");
            assertJson(expected, got);
        } catch (Exception e) {
            fail("exception thrown " + e.getLocalizedMessage());
        }
    }
}
