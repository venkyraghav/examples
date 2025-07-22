package io.confluent.flink.demo;

import java.util.List;

public class Item {
    private String name;
    private List<String> values;

    public Item() {}
    public Item(String name, List<String> values) {
        this.name = name;
        this.values = values;
    }

    // Getters and setters are required for SnakeYAML
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<String> getValues() { return values; }
    public void setValues(List<String> values) { this.values = values; }
}