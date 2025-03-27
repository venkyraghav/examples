package io.confluent.flink.demo;
import org.apache.flink.table.functions.ScalarFunction;
import org.apache.flink.util.StringUtils;
import org.json.*;

import java.util.Base64;

public class XML2JSON extends ScalarFunction {

    public String eval(byte[] xml) {
        if (xml == null)
            return eval("");
        return eval(Base64.getEncoder().encodeToString(xml));
    }
    public String eval(String xml) {
        try {
            if (StringUtils.isNullOrWhitespaceOnly(xml))
                return "{}";
            return XML.toJSONObject(xml).toString();
        }
        catch (JSONException e) {
            System.out.println(e.toString());
            return "{\"Exception\": \"" +  e.getLocalizedMessage() + "\"}";
        }
    }

}


