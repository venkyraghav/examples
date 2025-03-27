package io.confluent.flink.demo;

import org.apache.flink.table.annotation.DataTypeHint;
import org.apache.flink.table.functions.ScalarFunction;
import org.apache.flink.types.Row;

public class ValidateProductNamev3 extends ScalarFunction {
    @DataTypeHint("ROW<valid boolean, error String>")
    public Row eval(String text) {
        var words = text.split("\\W+");
        if (words.length != 3) {
            return Row.of(false, "Name has more than 3 words");
        } else {
            return Row.of(true, null);
        }

    }
}