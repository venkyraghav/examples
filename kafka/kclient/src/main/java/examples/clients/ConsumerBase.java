package examples.clients;

import java.io.IOException;
import java.util.Properties;

import org.apache.avro.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import examples.common.ConsumerCommand;

public abstract class ConsumerBase {
    private static final Logger log = LoggerFactory.getLogger(ConsumerBase.class);
    protected ConsumerCommand command;
    private org.apache.avro.Schema.Parser parser;
    private String avroKeySchemaString = "{\"type\":\"record\",\"name\":\"mykey\",\"fields\":[{\"name\":\"key\",\"type\":\"string\"}]}";
    private String avroValueSchemaString = "{\"type\":\"record\",\"name\":\"myrecord\",\"fields\":[{\"name\":\"f1\",\"type\":\"string\"}]}";
    private Schema avroKeySchema;
    private Schema avroValueSchema;

    public ConsumerBase(ConsumerCommand command) {
        this.command = command;
        this.parser = new org.apache.avro.Schema.Parser();

        avroKeySchema = parser.parse(avroKeySchemaString);
        avroValueSchema = parser.parse(avroValueSchemaString);
    }

    public void run() throws Exception {
        Properties props = command.getProperties();
        props.forEach(
                (k, v) -> {
                    if (log.isDebugEnabled()) {
                        log.debug("Key=" + k + ", Value=" + v);
                    }
                });
        doProcess(props);
    }

    protected abstract void doProcess(Properties props) throws IOException;
}
