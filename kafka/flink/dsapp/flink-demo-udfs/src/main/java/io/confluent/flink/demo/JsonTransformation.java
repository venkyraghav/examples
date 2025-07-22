package io.confluent.flink.demo;

import java.util.List;

import org.apache.flink.table.functions.ScalarFunction;
import org.apache.flink.util.StringUtils;

import com.bazaarvoice.jolt.Chainr;
import com.bazaarvoice.jolt.JsonUtils;
public class JsonTransformation extends ScalarFunction {

    public String eval(byte[] jsonIn, String joltSpecName) {
        if (jsonIn == null)
            return eval("", joltSpecName);
        return eval(new String(jsonIn), joltSpecName);
    }
    
    public String eval(String jsonIn, String joltSpecName) {
      if (StringUtils.isNullOrWhitespaceOnly(jsonIn))
          return "{}";

      List<Object> chainrSpecJSON = JsonUtils.classpathToList("/joltspec/" + joltSpecName + ".json");
      Chainr chainr = Chainr.fromSpec(chainrSpecJSON);
      Object inputJSON = JsonUtils.jsonToObject(jsonIn);
      return JsonUtils.toJsonString(chainr.transform(inputJSON));
    }

    public static void main(String[] args) {
        String json = """
                {
                  "rating": {
                    "primary": {
                      "value": 3
                    },
                    "quality": {
                      "value": 4
                    }
                  }
                }
                """;
        JsonTransformation j = new JsonTransformation();
        System.out.println("output is " + j.eval(json, "ratingsample"));
    }
}


