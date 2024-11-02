package examples.clients;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;
import java.util.function.BiConsumer;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.errors.RetriableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import examples.common.ConsumerCommand;

public class SimpleConsumer extends ConsumerBase {
    private static final Logger log = LoggerFactory.getLogger(SimpleProducer.class);

    public SimpleConsumer(ConsumerCommand command) {
        super(command);
        if (log.isInfoEnabled()) {log.info("Using SimpleConsumer");}
    }

    @SuppressWarnings("unused")
    private void handleException(RecordMetadata metadata, Exception ex) {
        if (ex != null) {
            if (log.isInfoEnabled()) {log.info("handleException");}
            if (ex instanceof RetriableException e) {

            }
            log.error("Exception on topic {}, partition {}", metadata.topic(), metadata.partition());
        }
    }

    protected void doProcess(Properties props) {
        try (final Consumer<Object, Object> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Arrays.asList(command.getTopic()));

            while (true) {
                ConsumerRecords<Object, Object> records = consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<Object, Object> record : records) {
                    Object key = record.key();
                    Object value = record.value();
                    System.out.println(String.format("Consumed event from topic %s: key = %-10s value = %s", command.getTopic(), key, value));
                }
            }
        }
    }
}
