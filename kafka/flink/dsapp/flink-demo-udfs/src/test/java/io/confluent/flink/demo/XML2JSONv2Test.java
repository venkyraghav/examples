package io.confluent.flink.demo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.apache.flink.table.functions.FunctionContext;
import org.apache.flink.util.InstantiationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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

    private XML2JSONv2 xml2jsonv2;

    private final String contentUrl = "https://test.rawcontent.com/";

    private final String jsonPayload = """
{
    "orderId": "ORD-100249",
    "customerId": "CUST-78905",
    "status": "SHIPPED",
    "currency": "USD",
    "orderTotal": "48.50",
    "items": {
      "item": [
        {
          "sku": "SKU-1003",
          "quantity": "1",
          "unitPrice": "27.50"
        },
        {
          "sku": "SKU-2003",
          "quantity": "1",
          "unitPrice": "21.00"
        }
      ]
    }
}
            """;
    private final String xmlPayload = """
<order><orderId>ORD-100249</orderId><customerId>CUST-78905</customerId><status>SHIPPED</status><currency>USD</currency><orderTotal>48.50</orderTotal><items><item><sku>SKU-1003</sku><quantity>1</quantity><unitPrice>27.50</unitPrice></item><item><sku>SKU-2003</sku><quantity>1</quantity><unitPrice>21.00</unitPrice></item></items></order>
            """;
    private final String xmlSchema = """
<?xml version="1.0" encoding="UTF-8"?>
<xsd:schema xmlns:xsd="http://www.w3.org/2001/XMLSchema"
            elementFormDefault="qualified">

    <xsd:element name="order">
        <xsd:complexType>
            <xsd:sequence>
                <xsd:element name="orderId" type="xsd:string"/>
                <xsd:element name="customerId" type="xsd:string"/>
                <xsd:element name="status" type="xsd:string"/>
                <xsd:element name="currency" type="xsd:string"/>
                <xsd:element name="orderTotal" type="xsd:decimal"/>
                <xsd:element name="items">
                    <xsd:complexType>
                        <xsd:sequence>
                            <xsd:element name="item" maxOccurs="unbounded">
                                <xsd:complexType>
                                    <xsd:sequence>
                                        <xsd:element name="sku" type="xsd:string"/>
                                        <xsd:element name="quantity" type="xsd:integer"/>
                                        <xsd:element name="unitPrice" type="xsd:decimal"/>
                                    </xsd:sequence>
                                </xsd:complexType>
                            </xsd:element>
                        </xsd:sequence>
                    </xsd:complexType>
                </xsd:element>
            </xsd:sequence>
        </xsd:complexType>
    </xsd:element>

</xsd:schema>
                """;

    @BeforeEach
    void setUp() throws Exception {
        xml2jsonv2 = new XML2JSONv2(httpClient);
        when(context.getJobParameter("GITHUB_RAWCONTENT.endpoint", null)).thenReturn(contentUrl);
        xml2jsonv2.open(context);
    }

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

    @Test
    void udfIsSerializable() throws Exception {
        XML2JSONv2 original = new XML2JSONv2();

        byte[] bytes;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(original);
            oos.flush();
            bytes = baos.toByteArray();
        }

        Object copy;
        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                ObjectInputStream ois = new ObjectInputStream(bais)) {
            copy = ois.readObject();
        }

        assertNotNull(copy);
    }

    @Test
    public void testSerializationCheck() {
        XML2JSONv2 udf = new XML2JSONv2();
        assertThat(InstantiationUtil.isSerializable(udf)).isTrue();
    }

    @Test
    public void testXmlStringNull() {
        String input = null;
        String expected = "";

        String got = new XML2JSONv2().eval(input, "test");

        assertJson(expected, got);
    }

    @Test
    public void testXmlStringEmpty() {
        String input = "";
        String expected = "";
        String got = new XML2JSONv2().eval(input, "test");

        assertJson(expected, got);
    }

    @Test
    public void testXmlByteNull() {
        byte[] input = null;
        String expected = "";
        String got = new XML2JSONv2().eval(input, "test");

        assertJson(expected, got);
    }

    @Test
    public void testXmlSchemaNameNull() {
        String input = xmlPayload;
        String expected = jsonPayload;
        try {
            String got = xml2jsonv2.eval(input, null);

            assertJson(expected, got);
        } catch (Exception e) {
            fail("exception thrown " + e.getLocalizedMessage());
        }
    }

    @Test
    public void testXmlSchemaEmpty() {
        String input = xmlPayload;
        String expected = jsonPayload; // change this to exception
        try {
            String got = xml2jsonv2.eval(input, "");

            assertJson(expected, got);
        } catch (Exception e) {
            fail("exception thrown " + e.getLocalizedMessage());
        }
    }

    @Test
    public void testXmlSchemaNameGood() throws IOException, InterruptedException {
        String input = xmlPayload;
        String expected = jsonPayload;

        try {
            when(httpResponse.statusCode()).thenReturn(200);
            when(httpResponse.body()).thenReturn(xmlSchema);
            when(httpClient.send(any(HttpRequest.class), any())).thenReturn(httpResponse);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        String got = xml2jsonv2.eval(input, "order.xsd");

        assertJson(expected, got);
    }

    @Test
    public void testXmlBytesSchemaNameGood() {
        String input = xmlPayload;
        String expected = jsonPayload;

        try {
            when(httpResponse.statusCode()).thenReturn(200);
            when(httpResponse.body()).thenReturn(xmlSchema);
            when(httpClient.send(any(HttpRequest.class), any())).thenReturn(httpResponse);
        }
        catch (Exception e) {}

        String got = xml2jsonv2.eval(input.getBytes(), "order.xsd");
        assertJson(expected, got);
    }

    @Test
    public void testXmlSchemaNameNotFound() {
        String input = xmlPayload;
        String expected = """
                {"Error":"schema is order2.xsd. refer other errors","url":"https://test.rawcontent.com/venkyraghav/restartgo/refs/heads/main/order2.xsd","statusCode":"404"}
                """;;

        try {
            when(httpResponse.statusCode()).thenReturn(404);
            when(httpClient.send(any(HttpRequest.class), any())).thenReturn(httpResponse);
        }
        catch (Exception e) {}

        String got = xml2jsonv2.eval(input.getBytes(), "order2.xsd");
        assertJson(expected, got);
    }
}
