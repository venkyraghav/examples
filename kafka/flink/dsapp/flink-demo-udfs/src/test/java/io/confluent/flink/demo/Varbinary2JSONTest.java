package io.confluent.flink.demo;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.flink.types.Row;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 *
 * @author venky
 */
public class Varbinary2JSONTest {
    private final static ObjectMapper mapper = new ObjectMapper();

    private Row getRow(String json) throws JsonProcessingException {
        if (StringUtils.isEmpty(json)) {
            return null;
        }
        Map<String, Object> map = mapper.readValue(json, Map.class);
        Row retRow = Row.withNames();
        map.forEach( (k,v) -> {
            retRow.setField(k, v);
        });
        return retRow;
    }

    private void assertRow(String expectedJson, Row actual) {
        try {
            assertThat(actual).isEqualTo(getRow(expectedJson));
        } catch (JsonProcessingException ex) {
            ex.printStackTrace();
            assertThat(false);
        }
    }

    @Test
    public void testBadJson() { // Missing "," for field `id`
        String input = """
            {
                "id": "1"
                "name": "John Doe",
                "age": "30"
            }
            """;
        assertThat(new Varbinary2JSON().eval(input, "tbl_1")).isNull();
    }

    @Test
    public void testEmptyJsonIn() {
        String input = """
            """;
        assertThat(new Varbinary2JSON().eval(input, "tbl_1")).isNull();
    }

    @Test
    public void testEmptyTblEntry() {
        String input = """
            {
                "gf1": "1",
                "gf2": "John Doe",
                "gf_tbl": "tbl_1",
                "tbl_1_f1": "value1",
                "tbl_1_f2": "value2",
                "tbl_1_f3": "value3"
            }
            """;
        assertThat(new Varbinary2JSON().eval(input, "")).isNull();
    }

    @Test
    public void testTblEntryNotExists() {
        String input = """
            {
                "gf1": "1",
                "gf2": "John Doe",
                "gf_tbl": "tbl_3",
                "tbl_3_f1": "value1",
                "tbl_3_f2": "value2",
                "tbl_3_f3": "value3"
            }
            """;
        assertThat(new Varbinary2JSON().eval(input, "tbl_3")).isNull();
    }

    @Test
    public void testTblEntryExistsByteArray() {
        String input = """
            {
                "gf1": "1",
                "gf2": "John Doe",
                "gf_tbl": "tbl_1",
                "tbl_1_f1": "value1",
                "tbl_1_f2": "value2",
                "tbl_1_f3": "value3"
            }
            """;
        String expected = """
            {
                "gf1": "1",
                "gf2": "John Doe",
                "gf_tbl": "tbl_1",
                "tbl_1_f1": "value1",
                "tbl_1_f2": "value2",
                "tbl_1_f3": "value3"
            }
            """;
        assertRow(expected, new Varbinary2JSON().eval(input.getBytes(), "tbl_1"));
    }

    @Test
    public void testTblEntryExists() {
        String input = """
            {
                "gf1": "1",
                "gf2": "John Doe",
                "gf_tbl": "tbl_1",
                "tbl_1_f1": "value1",
                "tbl_1_f2": "value2",
                "tbl_1_f3": "value3"
            }
            """;
        String expected = """
            {
                "gf1": "1",
                "gf2": "John Doe",
                "gf_tbl": "tbl_1",
                "tbl_1_f1": "value1",
                "tbl_1_f2": "value2",
                "tbl_1_f3": "value3"
            }
            """;
        assertRow(expected, new Varbinary2JSON().eval(input, "tbl_1"));
    }

    @Test
    public void testTblEntryExistsFor2Tbls() {
        String input = """
            {
                "gf1": "1",
                "gf2": "John Doe",
                "gf_tbl": "tbl_1",
                "tbl_1_f1": "value1",
                "tbl_1_f2": "value2",
                "tbl_1_f3": "value3"
            }
            """;
        String expected = """
            {
                "gf1": "1",
                "gf2": "John Doe",
                "gf_tbl": "tbl_1",
                "tbl_1_f1": "value1",
                "tbl_1_f2": "value2",
                "tbl_1_f3": "value3"
            }
            """;
        assertRow(expected, new Varbinary2JSON().eval(input, "tbl_1"));

        input = """
            {
                "gf1": "1",
                "gf2": "John Doe",
                "gf_tbl": "tbl_2",
                "tbl_2_f1": "value1",
                "tbl_2_f2": "value2"
            }
            """;
        expected = """
            {
                "gf1": "1",
                "gf2": "John Doe",
                "gf_tbl": "tbl_2",
                "tbl_2_f1": "value1",
                "tbl_2_f2": "value2"
            }
            """;
        assertRow(expected, new Varbinary2JSON().eval(input, "tbl_2"));
    }

    @Test
    public void testMoreColumnsInTblEntry() {
        String input = """
            {
                "gf1": "1",
                "gf2": "John Doe",
                "gf_tbl": "tbl_1",
                "tbl_1_f1": "value1",
                "tbl_1_f2": "value2"
            }
            """;
        String expected = """
            {
                "gf1": "1",
                "gf2": "John Doe",
                "gf_tbl": "tbl_1",
                "tbl_1_f1": "value1",
                "tbl_1_f2": "value2"
            }
            """;
        assertRow(expected, new Varbinary2JSON().eval(input, "tbl_1"));
    }

    @Test
    public void testMoreColumnsInInput() {
        String input = """
            {
                "gf1": "1",
                "gf2": "John Doe",
                "gf_tbl": "tbl_1",
                "tbl_1_f1": "value1",
                "tbl_1_f2": "value2",
                "tbl_1_f3": "value3",
                "tbl_1_f4": "value4"
            }
            """;
        String expected = """
            {
                "gf1": "1",
                "gf2": "John Doe",
                "gf_tbl": "tbl_1",
                "tbl_1_f1": "value1",
                "tbl_1_f2": "value2",
                "tbl_1_f3": "value3"
            }
            """;
        assertRow(expected, new Varbinary2JSON().eval(input, "tbl_1"));
    }
}