package examples;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.FencedInstanceIdException;
import org.apache.kafka.common.errors.ProducerFencedException;

import java.io.*;
import java.nio.file.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class MyProducer {
    private static boolean doConsume = true;
    private static String transactionalId = "";
    private static String groupInstanceId = "";
    private static String groupId = "";
    private static String clientId = "";
    private static AtomicLong messageRemaining = new AtomicLong(Long.MAX_VALUE);

    public static void main(final String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("Please provide the configuration file path as a command line argument");
            System.exit(1);
        }
        boolean isTransactional = false;
        if (args.length == 2) {
            isTransactional = true;
        }

        // Load producer configuration settings from a local file
        final Properties props = loadConfig(args[0]);
        props.forEach(
            (k,v) -> { System.out.println("Key=" + k + ", Value=" + v);}
        );
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down gracefully ...");
            doConsume = false;
        }));

        clientId = props.getProperty(ConsumerConfig.CLIENT_ID_CONFIG);
        if (clientId == null) {
            clientId = "noname-client";
            props.setProperty(ConsumerConfig.CLIENT_ID_CONFIG, clientId);
        }
        groupId = props.getProperty(ConsumerConfig.GROUP_ID_CONFIG);
        if (groupId == null) {
            groupId = "noname-group";
            props.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        }
        groupInstanceId = props.getProperty(ConsumerConfig.GROUP_INSTANCE_ID_CONFIG);
        if (groupInstanceId == null) {  
            groupInstanceId = groupId + "0";
            props.setProperty(ConsumerConfig.GROUP_INSTANCE_ID_CONFIG, groupInstanceId);
        }

        if (isTransactional) {
            transactionalId = props.getProperty(ProducerConfig.TRANSACTIONAL_ID_CONFIG);
            if (transactionalId == null) {
                transactionalId = "noname-txn-id";
                props.setProperty(ProducerConfig.TRANSACTIONAL_ID_CONFIG, transactionalId);
            }
            props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
            props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
            props.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, transactionalId);
            props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
            props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);
            props.put(ProducerConfig.ACKS_CONFIG, "all");

            transactionalProducer(props);
        } else {
            simpleProducer(props);
        }
    }

    private static long messagesRemaining(final KafkaConsumer<String, String> consumer) {
        final Map<TopicPartition, Long> fullEndOffsets = consumer.endOffsets(new ArrayList<>(consumer.assignment()));
        // If we couldn't detect any end offset, that means we are still not able to fetch offsets.
        if (fullEndOffsets.isEmpty()) {
            return Long.MAX_VALUE;
        }

        return consumer.assignment().stream().mapToLong(partition -> {
            long currentPosition = consumer.position(partition);
            printWithTxnId("Processing partition " + partition + " with full offsets " + fullEndOffsets);
            if (fullEndOffsets.containsKey(partition)) {
                return fullEndOffsets.get(partition) - currentPosition;
            }
            return 0;
        }).sum();
    }

    public static void transactionalProducer(Properties props) {
        try(KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
            KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {

            producer.initTransactions();
            consumer.subscribe(Arrays.asList("test_topic_5"), new ConsumerRebalanceListener() {
                @Override
                public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
                    printWithTxnId("Revoked partition assignment to kick-off rebalancing: " + partitions);
                }
    
                @Override
                public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
                    printWithTxnId("Received partition assignment after rebalancing: " + partitions);
                    messageRemaining.set(messagesRemaining(consumer));
                }
            });
            Duration duration = Duration.ofMillis(100);

            while(doConsume) {
                try {
                    ConsumerRecords<String, String> records = consumer.poll(duration);
                    if (records.isEmpty()) {
                        System.out.println("No records to process");
                        try {Thread.sleep(200);} catch(Exception e){}
                        continue;
                    }
                    producer.beginTransaction();
                    for (ConsumerRecord<String, String> record : records) {
                        System.out.println("Found key=" + record.key() + ", value=" + record.value());
                        
                        ProducerRecord<String, String> changeRecord = new ProducerRecord<>("test_topic_6", record.key(), record.value());
                        producer.send(changeRecord);
                    }

                    System.out.println("Committing");
                    Map<TopicPartition, OffsetAndMetadata> offsets = consumerOffsets(consumer);
                    producer.sendOffsetsToTransaction(offsets, consumer.groupMetadata());
                    producer.commitTransaction();
                    
                    // WARN [GroupMetadataManager brokerId=1] group: xx with leader: xx has received offset commits from consumers as well as transactional producers. 
                    // Mixing both types of offset commits will generally result in surprises and should be avoided.
                    // (kafka.coordinator.group.GroupMetadataManager)
                    // Below code can cause this WARN message
                    // consumer.commitSync();
                    // `isolation.level=read_uncommitted` also causes this
                } catch (ProducerFencedException e) {
                    throw new KafkaException(String.format("The transactional.id %s has been claimed by another process", transactionalId));
                } catch (FencedInstanceIdException e) {
                    throw new KafkaException(String.format("The group.instance.id %s has been claimed by another process", groupInstanceId));
                } catch (KafkaException e) {
                    // If we have not been fenced, try to abort the transaction and continue. This will raise immediately
                    // if the producer has hit a fatal error.
                    producer.abortTransaction();

                    // The consumer fetch position needs to be restored to the committed offset
                    // before the transaction started.
                    resetToLastCommittedPositions(consumer);
                }
            }
        }
    }

    private static Map<TopicPartition, OffsetAndMetadata> consumerOffsets(KafkaConsumer<String, String> consumer) {
        Map<TopicPartition, OffsetAndMetadata> offsets = new HashMap<>();
        for (TopicPartition topicPartition : consumer.assignment()) {
            offsets.put(topicPartition, new OffsetAndMetadata(consumer.position(topicPartition), null));
        }
        return offsets;
    }

    private static void printWithTxnId(final String message) {
        System.out.println(transactionalId + ": " + message);
    }

    private static void resetToLastCommittedPositions(KafkaConsumer<String, String> consumer) {
        final Map<TopicPartition, OffsetAndMetadata> committed = consumer.committed(consumer.assignment());
        consumer.assignment().forEach(tp -> {
            OffsetAndMetadata offsetAndMetadata = committed.get(tp);
            if (offsetAndMetadata != null)
                consumer.seek(tp, offsetAndMetadata.offset());
            else
                consumer.seekToBeginning(Collections.singleton(tp));
        });
    }

    public static void simpleProducer(Properties props) {
        final String topic = "purchases";

        String[] users = {"eabara", "jsmith", "sgarcia", "jbernard", "htanaka", "awalther"};
        String[] items = {"book", "alarm clock", "t-shirts", "gift card", "batteries"};
        try (final Producer<String, String> producer = new KafkaProducer<>(props)) {
            final Random rnd = new Random();
            final Long numMessages = 10L;
            for (Long i = 0L; i < numMessages; i++) {
                String user = users[rnd.nextInt(users.length)];
                String item = items[rnd.nextInt(items.length)];

                producer.send(
                        new ProducerRecord<>(topic, user, item),
                        (event, ex) -> {
                            if (ex != null)
                                ex.printStackTrace();
                            else
                                System.out.printf("Produced event to topic %s: key = %-10s value = %s%n", topic, user, item);
                        });
            }
            System.out.printf("%s events were produced to topic %s%n", numMessages, topic);
        }
    }

    // We'll reuse this function to load properties from the Consumer as well
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