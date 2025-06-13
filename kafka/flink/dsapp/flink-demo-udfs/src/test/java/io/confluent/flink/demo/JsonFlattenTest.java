package io.confluent.flink.demo;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonFlattenTest {

    private String minifyJson(String prettyJson) {
        if (prettyJson.isBlank()) {
            return "";
        }
        ObjectMapper mapper = new ObjectMapper();
        JsonNode tree;
        try {
            tree = mapper.readTree(prettyJson);
            return mapper.writeValueAsString(tree);
        } catch (JsonProcessingException ex) {
            return "{\"exception\":" + ex.getLocalizedMessage() + "}";
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
        assertThat(got).isEqualTo(minifyJson(expected));
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
        assertThat(got).isEqualTo(minifyJson(expected));
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
        assertThat(got).isEqualTo(minifyJson(expected));
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
        assertThat(got).isEqualTo(minifyJson(expected));
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
        assertThat(got).isEqualTo(minifyJson(expected));
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
        assertThat(got).isEqualTo(minifyJson(expected));
    }
}