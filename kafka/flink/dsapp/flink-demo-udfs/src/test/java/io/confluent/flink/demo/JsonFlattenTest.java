package io.confluent.flink.demo;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonFlattenTest {
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
    public void testBadJson() {
        String input = """
            {
                "id": "1"
                "name": "John Doe",
                "age": "30"
            }
            """;
        String expected = """
            """;

        String got = new JsonFlatten().eval(input);
        assertJson(expected, got);
    }

    @Test
    public void testSimpleJsonByteArray() {
        String input = """
            {
                "id": "1",
                "name": "John Doe",
                "age": "30"
            }
            """;
        String expected = """
            {
            "id": "1",
            "name": "John Doe",
            "age": "30"
            }
            """;

        String got = new JsonFlatten().eval(input.getBytes());
        assertJson(expected, got);
    }
    @Test
    public void testSimpleJson() {
        String input = """
            {
                "id": "1",
                "name": "John Doe",
                "age": "30"
            }
            """;
        String expected = """
            {
            "id": "1",
            "name": "John Doe",
            "age": "30"
            }
            """;

        String got = new JsonFlatten().eval(input);
        assertJson(expected, got);
    }

    @Test
    public void testFirstLevelJson() {
        String input = """
            {
            "id": 1,
            "name": {
                "first": "John",
                "last": "Doe"
            },
            "age": 30
        }
        """;
        String expected = """
            {
            "id": "1",
            "name.first": "John",
            "name.last": "Doe",
            "age": "30"
            }
            """;

        String got = new JsonFlatten().eval(input);
        assertJson(expected, got);
    }

    @Test
    public void testNestedJson() {
        String input = """
                    {
                    "id": 1,
                    "name": {
                        "first": "John",
                        "last": "Doe"
                    },
                    "address": {
                        "street": "123 Main St",
                        "city": "Anytown",
                        "state": "CA",
                        "zip": "12345"
                    },
                    "age": 30
                }
                """;

        String expected = """
                {
                "id": "1",
                "name.first": "John",
                "name.last": "Doe",
                "address.street": "123 Main St",
                "address.city": "Anytown",
                "address.state": "CA",
                "address.zip": "12345",
                "age": "30"
            }
            """;

        String got = new JsonFlatten().eval(input);
        assertJson(expected, got);
    }

    @Test
    public void testDeeplyNestedJson() {
        String input = """
                    {
                    "id": 1,
                    "name": {
                        "first": "John",
                        "last": "Doe"
                    },
                    "address": {
                        "street": {
                            "name": "Main St",
                            "number": "123"
                        },
                        "city": "Anytown",
                        "state": "CA",
                        "zip": "12345"
                    },
                    "contact": {
                        "email": "john.doe@example.com",
                        "phone": "555-1234"
                    },
                    "age": 30
                }
                """;

        String expected = """
                {
                "id": "1",
                "name.first": "John",
                "name.last": "Doe",
                "address.street.name": "Main St",
                "address.street.number": "123",
                "address.city": "Anytown",
                "address.state": "CA",
                "address.zip": "12345",
                "contact.email": "john.doe@example.com",
                "contact.phone": "555-1234",
                "age": "30"
            }
            """;

        String got = new JsonFlatten().eval(input);
        assertJson(expected, got);
    }


    @Test
    public void testFirstLevelJsonWithDelimiter() {
        String input = """
            {
            "id": 1,
            "name": {
                "first": "John",
                "last": "Doe"
            },
            "age": 30
        }
        """;
        String expected = """
            {
            "id": "1",
            "name_first": "John",
            "name_last": "Doe",
            "age": "30"
            }
            """;

        String got = new JsonFlatten().eval(input, "_");
        assertJson(expected, got);
    }
}