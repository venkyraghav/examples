package examples.common;

import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import examples.clients.ProducerBase;
import examples.clients.SimpleProducer;
import examples.common.pico.utils.FileExistsConverter;
import examples.common.pico.utils.FormatConverter;
import examples.common.pico.utils.PositiveNumberConverter;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "producer", version = "0.0.1", mixinStandardHelpOptions = true, usageHelpAutoWidth = true, showDefaultValues = true)
public class ProducerCommand extends ClientCommand {
    private static Logger log = LoggerFactory.getLogger(ProducerCommand.class);

    @Option(names = {"-b", "--bootstrap.server"}, paramLabel = "BOOTSTRAP.SERVER", description = "Kafka bootstrap server url", required = true)
    private String bootstrapServer;

    @Option(names = {"-c", "--command.config"}, paramLabel = "COMMAND.CONFIG", description = "File with Configuration Parameters to Kafka Client", required = false, converter = FileExistsConverter.class)
    private String commandConfig;

    @Option(names = {"-k", "--key.format"}, paramLabel = "KEY.FORMAT", description = "Format of the Kafka Key Record. Supported formats (string, avro, json)", required = false, defaultValue = "string", converter = FormatConverter.class)
    private Format keyFormat;

    @Option(names = {"-v", "--value.format"}, paramLabel = "VALUE.FORMAT", description = "Format of the Kafka Value Record. Supported formats (string, avro, json)", required = false, defaultValue = "string", converter = FormatConverter.class)
    private Format valueFormat;

    @Option(names = {"-t", "--topic"}, paramLabel = "TOPIC", description = "Topic name to produce", required = true)
    private String topic;

    @Option(names = {"-s", "--payload.size"}, paramLabel = "PAYLOAD.SIZE", description = "Size of the payload to generate > 0", required = false, defaultValue = "10", converter = PositiveNumberConverter.class)
    private int payloadSize;
    
    @Option(names = {"-n", "--payload.count"}, paramLabel = "PAYLOAD.COUNT", description = "Count of messages to generate > 0", required = false, defaultValue = "10", converter = PositiveNumberConverter.class)
    private int payloadCount;
    
    @Option(names = {"-x", "--transactional"}, paramLabel = "TRANSACTIONAL", description = "Use transactional producer", required = false, defaultValue = "false")
    private boolean transactional;

    private Properties props;

    private static final long finalSleep = 5_000;

    public Format getKeyFormat() {
        return keyFormat;
    }

    public Format getValueFormat() {
        return valueFormat;
    }

    public String getTopic() {
        return topic;
    }

    public int getPayloadSize() {
        return payloadSize;
    }

    public int getPayloadCount() {
        return payloadCount;
    }

    public Properties getProperties() {
        Properties retVal = new Properties();
        props.forEach((k,v) -> retVal.put(k, v));
        return retVal;
    }

    protected Integer process() throws Exception {
        if (props == null) {
            throw new ArgException("Properties not set");
        }
        
        ProducerBase producer;
        // if (transactional) {
        //     producer = new TransactionalProducer(config);
        // } else {
        producer = new SimpleProducer(this);
        // }
        producer.run();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (log.isInfoEnabled()) {log.info("Shutting down gracefully after sleeping for {}ms ...", finalSleep);}
            try {Thread.sleep(finalSleep);}catch (Exception ignored) {}
        }));

        try {if (log.isInfoEnabled()) {log.info("Sleeping {}ms", finalSleep);}Thread.sleep(finalSleep);}catch (Exception ignored) {}

        return 0;
    }

    protected void validate() throws ArgException {
        props = loadCommandConfig(commandConfig);

        if (Utils.isBlank(bootstrapServer)) {
            throw new ArgException("bootstrap.server is required");
        }
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);

        if (!props.containsKey(ProducerConfig.CLIENT_ID_CONFIG) || Utils.isBlank(ProducerConfig.CLIENT_ID_CONFIG)) {
            props.setProperty(ProducerConfig.CLIENT_ID_CONFIG, "noname-client");
        }

        if (transactional) {
            if (!props.containsKey(ProducerConfig.TRANSACTIONAL_ID_CONFIG) || Utils.isBlank(props.getProperty(ProducerConfig.TRANSACTIONAL_ID_CONFIG))) {
                props.setProperty(ProducerConfig.TRANSACTIONAL_ID_CONFIG, "noname-txn-id"); // TODO hardcoded value
            }
            // TODO override only if not set
            props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed"); // TODO hardcoded value
            props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
            props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
            props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);
            props.put(ProducerConfig.ACKS_CONFIG, "all"); // TODO hardcoded value
        }

        if (payloadCount <= 0) {
            throw new ArgException("payloadCount should be > 0");
        }
        if (payloadSize <= 0) {
            throw new ArgException("payloadSize should be > 0");
        }
        if (Utils.isBlank(topic)) {
            throw new ArgException("topic is required");
        }
        
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, keyFormat.getSerializer());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, valueFormat.getSerializer());
    }
}
