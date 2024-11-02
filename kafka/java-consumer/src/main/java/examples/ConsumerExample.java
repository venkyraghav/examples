package examples;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

import static org.apache.kafka.clients.CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.CommonClientConfigs.SECURITY_PROTOCOL_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.*;
import static org.apache.kafka.common.config.SaslConfigs.SASL_JAAS_CONFIG;
import static org.apache.kafka.common.config.SaslConfigs.SASL_MECHANISM;

public class ConsumerExample {

    public static void main(final String[] args) {

        final Properties props = new Properties() {{
            // User-specific properties that you must set
            put(BOOTSTRAP_SERVERS_CONFIG, "pkc-631q08.us-east-2.aws.confluent.cloud:9092");
            put(SASL_JAAS_CONFIG,         "org.apache.kafka.common.security.plain.PlainLoginModule required username='YIGW6BCXOD44U6EO' password='3kTySFlR1KCMNyWtfF2kzUD0gNgDL50GMNV6hAi3HhyZFduZEHZO52BIZAxUyJkw';");

            // Fixed properties
            put(KEY_DESERIALIZER_CLASS_CONFIG,   StringDeserializer.class.getCanonicalName());
            put(VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getCanonicalName());
            put(GROUP_ID_CONFIG,                 "venky-test-group2");
            put(CLIENT_ID_CONFIG,                 "venky-client");
            put(ENABLE_AUTO_COMMIT_CONFIG,        "false");
            put(AUTO_OFFSET_RESET_CONFIG,        "earliest");
            put(SECURITY_PROTOCOL_CONFIG,        "SASL_SSL");
            put(SASL_MECHANISM,                  "PLAIN");
        }};

        final String topic = "test";

        try (final Consumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Arrays.asList(topic));
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<String, String> record : records) {
                    String key = record.key();
                    String value = record.value();
                    System.out.println(
                            String.format("Consumed event from topic %s: key = %-10s value = %s", topic, key, value));
                }
            }
        }
    }

}