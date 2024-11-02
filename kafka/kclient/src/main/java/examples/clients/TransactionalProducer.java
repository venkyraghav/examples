package examples.clients;

import java.io.IOException;
import java.util.Properties;

import examples.common.ProducerCommand;

public class TransactionalProducer extends ProducerBase {

    public TransactionalProducer(ProducerCommand command){
        super(command);
    }
    @Override
    protected void doProcess(Properties props) throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'doProcess'");
    }
    /*
    private static final Logger log = LoggerFactory.getLogger(TransactionalProducer.class);

    public TransactionalProducer(MyProducerConfig config) {
        super(config);
        if (log.isInfoEnabled()) {log.info("Using TransactionalProducer");}
    }

    protected void doProcess(Properties props) {
        try(KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
            KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {

            producer.initTransactions();
            consumer.subscribe(List.of("test_topic_5"), new ConsumerRebalanceListener() {
                @Override
                public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
                    printWithTxnId("Revoked partition assignment to kick-off rebalancing: " + partitions);
                }

                @Override
                public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
                    printWithTxnId("Received partition assignment after rebalancing: " + partitions);
                    config.setMessageRemaining(messagesRemaining(consumer));
                }
            });
            Duration duration = Duration.ofMillis(100);

            while(config.isDoConsume()) {
                try {
                    ConsumerRecords<String, String> records = consumer.poll(duration);
                    if (records.isEmpty()) {
                        System.out.println("No records to process");
                        try {Thread.sleep(200);} catch(Exception ignored){}
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
                    throw new KafkaException(String.format("The transactional.id %s has been claimed by another process", config.getTransactionalId()));
                } catch (FencedInstanceIdException e) {
                    throw new KafkaException(String.format("The group.instance.id %s has been claimed by another process", config.getGroupInstanceId()));
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

    private long messagesRemaining(final KafkaConsumer<String, String> consumer) {
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

    private Map<TopicPartition, OffsetAndMetadata> consumerOffsets(KafkaConsumer<String, String> consumer) {
        Map<TopicPartition, OffsetAndMetadata> offsets = new HashMap<>();
        for (TopicPartition topicPartition : consumer.assignment()) {
            offsets.put(topicPartition, new OffsetAndMetadata(consumer.position(topicPartition), null));
        }
        return offsets;
    }

    private void resetToLastCommittedPositions(KafkaConsumer<String, String> consumer) {
        final Map<TopicPartition, OffsetAndMetadata> committed = consumer.committed(consumer.assignment());
        consumer.assignment().forEach(tp -> {
            OffsetAndMetadata offsetAndMetadata = committed.get(tp);
            if (offsetAndMetadata != null)
                consumer.seek(tp, offsetAndMetadata.offset());
            else
                consumer.seekToBeginning(Collections.singleton(tp));
        });
    }
    private void printWithTxnId(final String message) {
        if (log.isInfoEnabled()) {log.info("{} {}", config.getTransactionalId(), message);}
    }
*/
}
