package examples.clients;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.errors.RetriableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import examples.common.MyProducerConfig;

import java.util.Properties;
import java.util.function.BiConsumer;

public class SimpleProducer extends ProducerBase {
    private static final Logger log = LoggerFactory.getLogger(SimpleProducer.class);

    private BiConsumer<ProducerRecord<Object, Object>, RecordMetadata> consumerProducer;
    private BiConsumer<RecordMetadata, Exception> consumerException;

    public SimpleProducer(MyProducerConfig config) {
        super(config);
        setProducerCallback(this::produce, this::handleException);
        if (log.isInfoEnabled()) {log.info("Using SimpleProducer");}
    }

    private void handleException(RecordMetadata metadata, Exception ex) {
        if (ex != null) {
            if (log.isInfoEnabled()) {log.info("handleException");}
            if (ex instanceof RetriableException e) {

            }
            log.error("Exception on topic {}, partition {}", metadata.topic(), metadata.partition());
        }
    }

    private void produce(ProducerRecord<Object, Object> record, RecordMetadata metadata) {
        if (log.isInfoEnabled()) {log.info("produced {}", record);}
    }

    private void setProducerCallback(BiConsumer<ProducerRecord<Object, Object>, RecordMetadata> consumerProducer, BiConsumer<RecordMetadata, Exception> consumerException) {
        this.consumerProducer = consumerProducer;
        this.consumerException = consumerException;
    }

    protected void doProcess(Properties props) {
        try (final Producer<Object, Object> producer = new KafkaProducer<>(props)) {
            final long numMessages = 5L;
            for (long outer = 0; outer < config.getPayloadCount(); outer++) {
                for (long i = 0; i < numMessages; i++) {
                    ProducerRecord<Object, Object> record = new ProducerRecord<>(config.getTopic(), 
                            getKey(config.getKeyFormat(), 10), 
                            getValue(config.getValueFormat(), config.getPayloadSize()));
                    producer.send(
                            record,
                            (metadata, ex) -> {
                                if (ex != null) {
                                    consumerException.accept(metadata, ex);
                                } else {
                                    consumerProducer.accept(record, metadata);
                                }
                            });
                }
                if (log.isInfoEnabled()) {log.info("{} events were produced to topic {}", numMessages, config.getTopic());}
                try {Thread.sleep(100);} catch (InterruptedException ignored) {}
            }
        }
    }
}
