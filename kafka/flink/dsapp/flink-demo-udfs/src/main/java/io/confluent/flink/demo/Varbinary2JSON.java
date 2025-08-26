package io.confluent.flink.demo;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.flink.table.functions.ScalarFunction;
import org.apache.flink.types.Row;
import org.apache.flink.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class Varbinary2JSON extends ScalarFunction {
    private final static Logger log = LoggerFactory.getLogger(Varbinary2JSON.class);
    private final static Map tblCols = new HashMap<String, Item>();
    private final static ObjectMapper mapper = new ObjectMapper();

    public Row eval(byte[] input, String tblName) {
        if (input == null)
            return eval("", tblName);
        return eval(new String(input), tblName);
    }

    public Row eval(String jsonIn, String tblName) {
        try {
            if (StringUtils.isNullOrWhitespaceOnly(jsonIn)) {
                throw new IllegalArgumentException("jsonIn is empty");
            }
            if (StringUtils.isNullOrWhitespaceOnly(tblName)) {
                throw new IllegalArgumentException("tblName is empty");
            }
            
            Map<String, Object> map = mapper.readValue(jsonIn, Map.class);
            Row retRow = Row.withNames();

            // if not initialized, read properties file, the table entry and populate `tblCols`
            // get tblName entry
            Item tblEntry = getTableEntry(tblName);

            // iterate over the list to create json object
            for (String columnName : tblEntry.getValues()) {
                Object columnValue = map.get(columnName);
                if (columnValue != null) {
                    retRow.setField(columnName, columnValue);
                }
            }
            return retRow;
        }
        catch (IOException|IllegalArgumentException|UnsupportedOperationException e) {
            log.error("Exception while parsing", e);
            return null;
        }
    }

    private void populateCache() throws IOException {
        if (!tblCols.isEmpty()) { // already populated
            if (log.isDebugEnabled()) {log.debug("Reusing Cache...");}
            return;
        }

        if (log.isDebugEnabled()) {log.debug("Populating Cache...");}
        String relativePath = "tblspec/tbl_colmapping.yaml";
        try (InputStream in = Varbinary2JSON.class.getClassLoader().getResourceAsStream(relativePath)) {
            if (in == null) {
                throw new IllegalArgumentException("File not found on classpath: " + relativePath);
            }
            ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
            List<Item> items = yamlMapper.readValue(in, new TypeReference<List<Item>>() {});
            for (Item item : items) {
                if (log.isDebugEnabled()) {log.debug("  Name: " + item.getName());}
                if (log.isDebugEnabled()) {log.debug("  Values: " + item.getValues());}
                tblCols.put(item.getName(), item);
            }
        }
    }

    private Item getTableEntry(String tblName) throws IOException {
        populateCache();
        Item item = (Item)tblCols.get(tblName);
        if (item == null) {
            throw new IllegalArgumentException("missing table name entry for " + tblName);
        }
        return item;
    }

}


