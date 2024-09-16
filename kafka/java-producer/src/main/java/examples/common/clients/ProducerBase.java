package examples.common.clients;

import examples.common.MyProducerConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.Random;

public abstract class ProducerBase {
    private static final Logger log = LoggerFactory.getLogger(ProducerBase.class);
    protected MyProducerConfig config;

    public ProducerBase(MyProducerConfig config) {
        this.config = config;
    }

    public void run() throws Exception {
        Properties props = loadConfig();
        props.forEach(
                (k,v) -> { if (log.isDebugEnabled()) {log.debug("Key=" + k + ", Value=" + v);}}
        );
        doProcess(props);
    }
    protected abstract void doProcess(Properties props) throws IOException;
    protected String generateRandonString(long targetStringLength) {
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        Random random = new Random();

        return random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    private Properties loadConfig() throws IOException {
        Properties props = new Properties();
        if (config.getCommandConfig() != null && Utils.isBlank(config.getCommandConfig())) {

            if (!Files.exists(Paths.get(config.getCommandConfig()))) {
                throw new IOException(config.getCommandConfig() + " not found.");
            }
            try (InputStream inputStream = new FileInputStream(config.getCommandConfig())) {
                props.load(inputStream);
            }
        }
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getBootstrapServer());
        config.setClientId(props.getProperty(ConsumerConfig.CLIENT_ID_CONFIG));
        if (config.getClientId() == null || Utils.isBlank(config.getClientId())) {
            config.setClientId("noname-client");
            props.setProperty(ConsumerConfig.CLIENT_ID_CONFIG, config.getClientId());
        }
        String groupId = props.getProperty(ConsumerConfig.GROUP_ID_CONFIG);
        if (groupId == null) {
            groupId = "noname-group";
            props.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        }
        config.setGroupInstanceId (props.getProperty(ConsumerConfig.GROUP_INSTANCE_ID_CONFIG));
        if (config.getGroupInstanceId() == null || Utils.isBlank(config.getGroupInstanceId())) {
            config.setGroupInstanceId (groupId + "0");
            props.setProperty(ConsumerConfig.GROUP_INSTANCE_ID_CONFIG, config.getGroupInstanceId());
        }

        if (config.isTransactional()) {
            config.setTransactionalId (props.getProperty(ProducerConfig.TRANSACTIONAL_ID_CONFIG));
            if (config.getTransactionalId() == null || Utils.isBlank(config.getTransactionalId())) {
                config.setTransactionalId ("noname-txn-id");
                props.setProperty(ProducerConfig.TRANSACTIONAL_ID_CONFIG, config.getTransactionalId());
            }
            props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
            props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
            props.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, config.getTransactionalId());
            props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
            props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);
            props.put(ProducerConfig.ACKS_CONFIG, "all");
        }
        if (!props.containsKey(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG)) {
            props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        }
        if (!props.containsKey(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG)) {
            props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        }

        return props;
    }

}
