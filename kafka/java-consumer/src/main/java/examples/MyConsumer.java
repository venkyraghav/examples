package examples;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.common.TopicPartition;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;

class SaveOffsetsOnRebalance implements ConsumerRebalanceListener {
    private Consumer<?,?> consumer;

    public SaveOffsetsOnRebalance(Consumer<?,?> consumer) {
        this.consumer = consumer;
    }

    public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
        System.out.println("called onPartitionsRevoked");
    }

    public void onPartitionsLost(Collection<TopicPartition> partitions) {
        System.out.println("called onPartitionsLost");
        // do not need to save the offsets since these partitions are probably owned by other consumers already
    }

    public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
        System.out.println("called onPartitionsAssigned");
    }
}

public class MyConsumer {

    public static void main2(final String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("Please provide the configuration file path as a command line argument");
            System.exit(1);
        }
        boolean dynamicMembership = true;
        int part = 0;
        if (args.length == 2) {
            dynamicMembership = false;
            part = Integer.parseInt(args[1]);
        }

        final String topic = "purchases";

        // Load consumer configuration settings from a local file
        // Reusing the loadConfig method from the ProducerExample class
        final Properties props = MyConsumer.loadConfig(args[0]);
        props.forEach(
            (k,v) -> { System.out.println("Key=" + k + ", Value=" + v);}
        );

        // Add additional properties.
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "kafka-java-getting-started");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        if (dynamicMembership) {
            System.out.println("Using Dyamic Membership");
            try (final Consumer<String, String> consumer = new KafkaConsumer<>(props)) {
                consumer.subscribe(Arrays.asList(topic), new SaveOffsetsOnRebalance(consumer));
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
        } else {
            System.out.println("Using Static Membership to partition " + part);
            try (final Consumer<String, String> consumer = new KafkaConsumer<>(props)) {
                TopicPartition partition = new TopicPartition(topic, part);
                consumer.assign(Arrays.asList(partition));
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

    public static Properties loadConfig(final String configFile) throws IOException {
        if (!Files.exists(Paths.get(configFile))) {
            throw new IOException(configFile + " not found.");
        }
        final Properties cfg = new Properties();
        try (InputStream inputStream = new FileInputStream(configFile)) {
            cfg.load(inputStream);
        }
        return cfg;
    }

}
