package io.confluent.examples.fincard.streams;

import io.confluent.examples.fincard.TransactionRequest;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.reflect.ReflectData;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KeyValueMapper;
import org.apache.kafka.streams.kstream.Produced;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import scala.sys.Prop;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;

@Service
public class FinCardSpendStream {
    private final static Logger log = LoggerFactory.getLogger(FinCardSpendStream.class);

    @Value("${kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${kafka.schema.registry.url}")
    private String schemaRegistryUrl;

    @Value("${spring.kafka.streams.application-id}")
    private String applicationId;

    @Value("${spring.kafka.streams.group-id}")
    private String groupId;

    @Value("${spring.kafka.streams.default.key.serde}")
    private String keySerde;

    @Value("${spring.kafka.streams.default.value.serde}")
    private String valueSerde;

    private final int cardinal_type = 0;
    private final int cardinal_amount = 1;
    private final int cardinal_currency = 2;
    private final int cardinal_customername = 3;
    private final int cardinal_zipcode = 4;
    private final int cardinal_cardnumber = 5;
    private final int cardinal_expdate = 6;
    private final int cardinal_pin = 7;
    private final int cardinal_vendorid = 8;
    private final int cardinal_deviceid = 9;
    private final int cardinal_datetime = 10;
    private final int cardinal_location = 11;
    private final int cardinal_nonce = 12;

    private Properties getStreamProperties(String suffix) {
        Properties streamsConfiguration = new Properties();
        streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, groupId + "-" + suffix);
        streamsConfiguration.put(StreamsConfig.CLIENT_ID_CONFIG, applicationId + "-" + suffix);
        streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        streamsConfiguration.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
        // Specify default (de)serializers for record keys and for record values.
        streamsConfiguration.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        streamsConfiguration.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, valueSerde);
        streamsConfiguration.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        streamsConfiguration.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 10 * 1000);

        return streamsConfiguration;
    }

    private void startStream(StreamsBuilder builder, Properties streamsConfiguration) {
        final KafkaStreams streams = new KafkaStreams(builder.build(), streamsConfiguration);
        streams.cleanUp();
        streams.start();

        // Add shutdown hook to respond to SIGTERM and gracefully close Kafka Streams
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                streams.close();
            }
        }));
    }

    @Bean
    public void justCopy() {
        String fromTopic = "test2";
        String toTopic = "transaction2";

        if (log.isInfoEnabled()) {log.info("justCopy stream started from %s to %s", fromTopic, toTopic);}

        Properties streamsConfiguration = getStreamProperties("copy");

        final StreamsBuilder builder = new StreamsBuilder();
        final KStream<String, SpecificRecord> stream = builder.stream(fromTopic);
        stream.to(toTopic);

        startStream(builder, streamsConfiguration);
    }

    // @Bean
    public void spendByZipcodeOld() {
        String fromTopic = "test2";
        String toTopic = "spendbyzipcodeold";

        if (log.isInfoEnabled()) {log.info("spendByZipcodeOld stream started from %s to %s", fromTopic, toTopic);}

        Properties streamsConfiguration = getStreamProperties("zipcode");
        final Serde<String> stringSerde = Serdes.String();
        final Serde<TransactionRequest> transactionRequestSerde = new SpecificAvroSerde<TransactionRequest>();

        final StreamsBuilder builder = new StreamsBuilder();
        final KStream<String, SpecificRecord> stream = builder.stream(fromTopic);
        final KStream<String, SpecificRecord> zipcodeRekey = stream.map( //thaat is part of tihs stream, setting key to zipcode
                (KeyValueMapper<String, SpecificRecord, KeyValue<String, SpecificRecord>>) (s, specificRecord)
                        -> new KeyValue<>(specificRecord.get(cardinal_zipcode).toString(), specificRecord)//this creates a kafkatopic record
        );
        zipcodeRekey.to(toTopic);//persist stream to the topic (rekey operation)

        startStream(builder, streamsConfiguration);
    }

    @Bean
    public void spendByZipcode() {
        String fromTopic = "test2";
        String toTopic = "spendbyzipcode";

        if (log.isInfoEnabled()) {log.info("spendByZipcode stream started from %s to %s", fromTopic, toTopic);}

        Properties streamsConfiguration = getStreamProperties("zipcode");
        final Map<String, String> serdeConfig = Collections.singletonMap(
                AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
        // streamsConfiguration.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, GenericAvroSerde.class);
        final Serde<GenericRecord> genericAvroSerde = new GenericAvroSerde();
        genericAvroSerde.configure(serdeConfig, true);
        final Serde<SpecificRecord> transactionRequestSerde = new SpecificAvroSerde<SpecificRecord>();
        transactionRequestSerde.configure(serdeConfig, false);

        final StreamsBuilder builder = new StreamsBuilder();
        final KStream<String, SpecificRecord> stream = builder.stream(fromTopic);
        final KStream<GenericRecord, SpecificRecord> zipcodeRekey = stream.map(
                (transactionId, transactionRecord) -> {
                    LocationGroup locationGroup = new LocationGroup();
                    locationGroup.setCardnumber(transactionRecord.get(cardinal_cardnumber).toString());
                    locationGroup.setZipcode(transactionRecord.get(cardinal_zipcode).toString());

                    GenericRecord record = mapObjectToRecord(locationGroup);
                    return new KeyValue<GenericRecord, SpecificRecord>(record, transactionRecord);
                }
        );
        zipcodeRekey.to(toTopic, Produced.with(genericAvroSerde, transactionRequestSerde));

        startStream(builder, streamsConfiguration);
    }

    private GenericData.Record mapObjectToRecord(Object object) {
        Assert.notNull(object, "object must not be null");
        final Schema schema = ReflectData.get().getSchema(object.getClass());
        final GenericData.Record record = new GenericData.Record(schema);
        schema.getFields().forEach(r -> record.put(r.name(), PropertyAccessorFactory.forDirectFieldAccess(object).getPropertyValue(r.name())));
        return record;
    }

    private <T> T mapRecordToObject(GenericData.Record record, T object) {
        Assert.notNull(record, "record must not be null");
        Assert.notNull(object, "object must not be null");
        final Schema schema = ReflectData.get().getSchema(object.getClass());
        Assert.isTrue(schema.getFields().equals(record.getSchema().getFields()), "Schema fields didn't match");
        record.getSchema().getFields().forEach(d -> PropertyAccessorFactory.forDirectFieldAccess(object).setPropertyValue(d.name(), record.get(d.name()) == null ? record.get(d.name()) : record.get(d.name()).toString()));
        return object;
    }
}

class LocationGroup {
    public String getZipcode() {
        return zipcode;
    }

    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }

    public String getCardnumber() {
        return cardnumber;
    }

    public void setCardnumber(String cardnumber) {
        this.cardnumber = cardnumber;
    }

    private String zipcode;
    private String cardnumber;

}
