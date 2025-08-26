package io.confluent.flink.demo;

import org.apache.flink.table.annotation.DataTypeHint;
import org.apache.flink.table.annotation.FunctionHint;
import org.apache.flink.table.functions.TableFunction;
import org.apache.flink.types.Row;

@FunctionHint(output = @DataTypeHint("ROW<valid boolean, error String>"))
public class ValidateProductNamev2 extends TableFunction<Row> {
    public static final String NAME = "VALIDATE_PRODUCT_NAME";

    public void eval(String text) {
        var words = text.split("\\W+");
        if (words.length != 3) {
            collect(Row.of(false, "Name has more than 3 words"));
        } else {
            collect(Row.of(true, null));
        }
    }
}