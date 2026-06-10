import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Properties;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import org.apache.kafka.clients.consumer.ConsumerConfig;

final class EiapConsumerConfig {

    private static final String DEFAULT_CONFIG_RESOURCE = "/consumer.properties";
    private static final String CONFIG_PATH_PROPERTY = "eiap.config";
    private static final String CONFIG_PATH_ENV = "EIAP_CONFIG";

    private final Properties properties;

    private EiapConsumerConfig(Properties properties) {
        this.properties = properties;
    }

    static EiapConsumerConfig load() throws IOException {
        Properties properties = new Properties();
        loadDefaults(properties);
        loadExternalOverrides(properties);
        applySystemPropertyOverrides(properties);
        applyEnvironmentOverrides(properties);
        validate(properties);
        return new EiapConsumerConfig(properties);
    }

    private static void loadDefaults(Properties properties) throws IOException {
        try (InputStream inputStream = EiapConsumerConfig.class.getResourceAsStream(DEFAULT_CONFIG_RESOURCE)) {
            if (inputStream == null) {
                throw new IOException("Missing classpath resource " + DEFAULT_CONFIG_RESOURCE);
            }
            properties.load(inputStream);
        }
    }

    private static void loadExternalOverrides(Properties properties) throws IOException {
        String externalPath = firstNonBlank(System.getProperty(CONFIG_PATH_PROPERTY), System.getenv(CONFIG_PATH_ENV));
        if (externalPath == null) {
            return;
        }

        try (InputStream inputStream = Files.newInputStream(Path.of(externalPath))) {
            Properties external = new Properties();
            external.load(inputStream);
            properties.putAll(external);
        }
    }

    private static void applySystemPropertyOverrides(Properties properties) {
        System.getProperties().forEach((key, value) -> {
            if (!(key instanceof String stringKey) || value == null) {
                return;
            }
            //if (stringKey.startsWith("consumer.") || stringKey.startsWith("schema.registry.")) {
            properties.setProperty(stringKey, value.toString());
            //}
        });
    }

    private static Optional<String> getKafkaEnv(String envKey) {
        if (envKey.startsWith("KAFKA_")) {
            return Optional.ofNullable(envKey.substring("KAFKA_".length()).toLowerCase().replace('_', '.'));
        } else if (envKey.startsWith("SCHEMA_REGISTRY_")) {
            return Optional.ofNullable(envKey.substring("SCHEMA_REGISTRY_".length()).toLowerCase().replace('_', '.'));
        } else if (envKey.startsWith("CONSUMER_")) {
            return Optional.ofNullable(envKey.substring("CONSUMER_".length()).toLowerCase().replace('_', '.'));
        } else if (envKey.startsWith("PRODUCER_")) {
            return Optional.ofNullable(envKey.substring("PRODUCER_".length()).toLowerCase().replace('_', '.'));
        }
        return Optional.empty();
    }

    private static void applyEnvironmentOverrides(Properties properties) {
        System.getenv().forEach((envKey, envValue) -> {
            if (envValue == null || envValue.isBlank()) {
                return;
            }
            Optional<String> key = getKafkaEnv(envKey);

            if (key.isPresent()) {
                properties.setProperty(key.get(), envValue);
            }
            if (envKey.startsWith("KAFKA_") || envKey.startsWith("SCHEMA_REGISTRY_") || envKey.startsWith("CONSUMER_")) {
                String propertyKey = envKey.toLowerCase().replace('_', '.');
                properties.setProperty(propertyKey, envValue);
            }
        });
    }

    private static void validate(Properties properties) {
        require(properties, ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG);
        require(properties, ConsumerConfig.GROUP_ID_CONFIG);
        require(properties, "schema.registry.url");
    }

    private static void require(Properties properties, String key) {
        if (isBlank(properties.getProperty(key))) {
            throw new IllegalArgumentException("Missing required property: " + key);
        }
    }

    Properties toConsumerProperties() {
        Properties consumerProperties = new Properties();
        properties.forEach((key, value) -> {
            if (key instanceof String stringKey && value != null) {
                consumerProperties.put(stringKey, value);
            }
        });
        return consumerProperties;
    }

    String topic() {
        return properties.getProperty("topic");
    }

    Duration pollTimeout() {
        long pollTimeoutMs = Long.parseLong(properties.getProperty("poll.timeout.ms", "5000"));
        return Duration.ofMillis(pollTimeoutMs);
    }

    @SuppressWarnings("unused")
    private void copyIfPresent(String key, Properties target) {
        String value = properties.getProperty(key);
        if (!isBlank(value)) {
            target.put(key, value);
        }
    }

    @SuppressWarnings("unused")
    private void copyIfPresent(String sourceKey, String targetKey, Properties target) {
        String value = properties.getProperty(sourceKey);
        if (!isBlank(value)) {
            target.put(targetKey, value);
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank() || Objects.equals(value.trim(), "changeme");
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
