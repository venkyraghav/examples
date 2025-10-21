import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class Main {
    public static void main(String[] args) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        // mapper.getFactory().enable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN);
        boolean writeBigdecimalAsPlain = true;

        if (writeBigdecimalAsPlain) {
            mapper.enable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN);

            SimpleModule module = new SimpleModule();
            PlainFloatSerializer plainFloatSerializer = new PlainFloatSerializer();
            module.addSerializer(Float.class, plainFloatSerializer);
            module.addSerializer(float.class, plainFloatSerializer);
            PlainDoubleSerializer plainDoubleSerializer = new PlainDoubleSerializer();
            module.addSerializer(Double.class, plainDoubleSerializer);
            module.addSerializer(double.class, plainDoubleSerializer);
            mapper.registerModule(module);
        }


        Map<String, Object> data = new HashMap<>();
        data.put("BigDecimal", new BigDecimal("1e-7"));
        data.put("Float32", Float.parseFloat("1e-7"));
        data.put("Double", Double.parseDouble("1e-7"));

        // String json = mapper.writeValueAsString(data);
        byte[] json = mapper.writeValueAsBytes(data);
        System.out.println("---");
        System.out.println(new String(json));  // Output: {"value":1.234567890123456E+9}
        System.out.println("---");
    }
}

class PlainFloatSerializer extends com.fasterxml.jackson.databind.JsonSerializer<Float> {
    @Override
    public void serialize(Float value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        // Convert float to BigDecimal and write as plain string
        gen.writeNumber(new BigDecimal(String.valueOf(value)).toPlainString());
    }
}

class PlainDoubleSerializer extends com.fasterxml.jackson.databind.JsonSerializer<Double> {
    @Override
    public void serialize(Double value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        // Convert float to BigDecimal and write as plain string
        gen.writeNumber(new BigDecimal(String.valueOf(value)).toPlainString());
    }
}

// class MyJsonSerializer <F extends Number> implements Serializers<JsonNode> {
//     @Override
//     public void serialize(Double value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
//         // Convert float to BigDecimal and write as plain string
//         gen.writeNumber(new BigDecimal(String.valueOf(value)).toPlainString());
//     }
// }
