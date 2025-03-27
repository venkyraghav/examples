package io.confluent.flink.demo;

import java.util.ArrayList;
import java.util.List;

import org.apache.flink.table.functions.ScalarFunction;

public class GenerateSeries extends ScalarFunction {
    private List<Long> genList(Long from, Long to) {
        List<Long> arr = new ArrayList<>();
        for (Long l = from; l < to; l++) {
            arr.add(l);
        }
        return arr;
    }

    // take any data type and return INT
    public List<Long> eval(Long from, Long to) {
        if (from > to) {
            return genList(to, from);
        }
        return genList(from, to);
    }
    
}
