package examples.common;

import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import examples.clients.ConsumerBase;
import examples.clients.SimpleConsumer;
import examples.common.pico.utils.FileExistsConverter;
import examples.common.pico.utils.FormatConverter;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "consumer", version = "0.0.1", mixinStandardHelpOptions = true, usageHelpAutoWidth = true, showDefaultValues = true)
public class ConsumerCommand extends ClientCommand {
    private static Logger log = LoggerFactory.getLogger(ConsumerCommand.class);

    @Option(names = {"-b", "--bootstrap.server"}, paramLabel = "BOOTSTRAP.SERVER", description = "Kafka bootstrap server url", required = true)
    private String bootstrapServer;

    @Option(names = {"-c", "--command.config"}, paramLabel = "COMMAND.CONFIG", description = "File with Configuration Parameters to Kafka Client", required = false, converter = FileExistsConverter.class)
    private String commandConfig;

    @Option(names = {"-k", "--key.format"}, paramLabel = "KEY.FORMAT", description = "Format of the Kafka Key Record. Supported formats (string, avro, json)", required = false, defaultValue = "string", converter = FormatConverter.class)
    private Format keyFormat;

    @Option(names = {"-v", "--value.format"}, paramLabel = "VALUE.FORMAT", description = "Format of the Kafka Value Record. Supported formats (string, avro, json)", required = false, defaultValue = "string", converter = FormatConverter.class)
    private Format valueFormat;

    @Option(names = {"-t", "--topic"}, paramLabel = "TOPIC", description = "Topic name to consume", required = true)
    private String topic;
    
    @Option(names = {"-B", "--from-beginning"}, paramLabel = "FROM.BEGINNING", description = "Consume from beginning", required = false, defaultValue = "false")
    private boolean fromBeginning;

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

    public Properties getProperties() {
        Properties retVal = new Properties();
        props.forEach((k,v) -> retVal.put(k, v));
        return retVal;
    }

    protected Integer process() throws Exception {
        if (props == null) {
            throw new ArgException("Properties not set");
        }
        
        ConsumerBase consumer;
        // if (transactional) {
        //     producer = new TransactionalProducer(config);
        // } else {
        consumer = new SimpleConsumer(this);
        // }
        consumer.run();

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
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);

        if (!props.containsKey(ConsumerConfig.GROUP_ID_CONFIG) || Utils.isBlank(ConsumerConfig.GROUP_ID_CONFIG)) {
            props.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "noname-group");
        }
        if (!props.containsKey(ConsumerConfig.CLIENT_ID_CONFIG) || Utils.isBlank(ConsumerConfig.CLIENT_ID_CONFIG)) {
            props.setProperty(ConsumerConfig.CLIENT_ID_CONFIG, "noname-client");
        }

        if (transactional) {
            // TODO override only if not set
            props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed"); // TODO hardcoded value
            props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        }

        if (Utils.isBlank(topic)) {
            throw new ArgException("topic is required");
        }
        
        if (fromBeginning) {
            props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        } else {
            props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        }
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, keyFormat.getDeserializer());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, valueFormat.getDeserializer());
    }
}
