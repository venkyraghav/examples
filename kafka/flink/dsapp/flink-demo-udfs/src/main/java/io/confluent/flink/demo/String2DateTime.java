package io.confluent.flink.demo;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.flink.table.functions.ScalarFunction;
import org.apache.flink.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class String2DateTime extends ScalarFunction {
    private final static Logger log = LoggerFactory.getLogger(String2DateTime.class);

    private final static SimpleDateFormat dtyyyyMMdd = new SimpleDateFormat("yyyyMMdd");
    private final static SimpleDateFormat dtyyyyMMddWithDash = new SimpleDateFormat("yyyy-MM-dd");
    private final static SimpleDateFormat dtyyyyMMddhhmmssSSS = new SimpleDateFormat("yyyyMMdd hhmmss.SSS");
    private final static SimpleDateFormat dtyyyyMMddhhmmssSSSWithDash = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");

    public Date eval(byte[] array) {
        if (array == null)
            return null;
        return eval(new String(array));
    }

    public Date eval(String strDateTime) {
        if (StringUtils.isNullOrWhitespaceOnly(strDateTime))
            return null;
        int len = strDateTime.length();

        try {
            if (len == 8 && strDateTime.matches("^\\d{8}$")) { // If the length is 8 and it's all digits, assume 'yyyyMMdd'
                return dtyyyyMMdd.parse(strDateTime);
            } else if (len == 10 && strDateTime.matches("^\\d{4}-\\d{2}-\\d{2}$")) { // If the length is 10 and matches the hyphenated pattern 'yyyy-MM-dd'
                return dtyyyyMMddWithDash.parse(strDateTime);
            } else if (len == 19 && strDateTime.matches("^\\d{4}\\d{2}\\d{2} \\d{2}\\d{2}\\d{2}\\.\\d{3}$")) { // If the length is 19 and matches the hyphenated pattern 'yyyyMMdd hhmmss.SSS'
                return dtyyyyMMddhhmmssSSS.parse(strDateTime);
            } else if (len == 23 && strDateTime.matches("^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{3}$")) { // If the length is 23 and matches the hyphenated pattern 'yyyy-MM-dd hh:mm:ss.SSS'
                return dtyyyyMMddhhmmssSSSWithDash.parse(strDateTime);
            } else if (strDateTime.matches("^\\d+(\\.\\d+)?$")) {
                switch (len) {
                    case 10 -> {
                        // if numeric assume epoch timestamp in seconds
                        return new Date(Long.parseLong(strDateTime) * 1_000L);
                    }
                    case 13 -> {
                        // if numeric assume epoch timestamp in millis
                        return new Date(Long.parseLong(strDateTime));
                    }
                    case 14 -> {
                        // if numeric assume epoch timestamp in seconds with millis in decimal
                        return new Date((long)(Double.parseDouble(strDateTime) * 1_000L));
                    }
                    case 16 -> {
                        // if numeric assume epoch timestamp in micros
                        return new Date(Long.parseLong(strDateTime) / 1_000L);
                    }
                    case 17 -> {
                        // if numeric assume epoch timestamp in milliseconds with micros in decimal (ignoring precision)
                        return new Date((long)(Double.parseDouble(strDateTime)));
                    }
                    default -> {
                        return null;
                    }
                }
            }
        } catch (ParseException e) {
            log.error("Exception while parsing " + strDateTime, e);
        }

        return null;
    }

}
