package io.confluent.examples.fincard.streams;

import io.confluent.examples.fincard.TransactionRequest;
import io.confluent.examples.fincard.model.CardReason;
import io.confluent.examples.fincard.model.CustomerAmountLocation;
import io.confluent.examples.fincard.model.CustomerVendorLocation;
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
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.Duration;
import java.util.*;

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
    private final int cardinal_reason = 13;


    public Properties getStreamProperties(String suffix) {
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

    private Topology startStream(StreamsBuilder builder, Properties streamsConfiguration) {
        Topology topology = builder.build();
        final KafkaStreams streams = new KafkaStreams(topology, streamsConfiguration);
        streams.cleanUp();
        streams.start();

        // Add shutdown hook to respond to SIGTERM and gracefully close Kafka Streams
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                streams.close();
            }
        }));
        return topology;
    }

    @Bean
    public Topology justCopy() {
        String fromTopic = "transactions";
        String toTopic = "transaction2";

        if (log.isInfoEnabled()) {log.info("justCopy stream started from %s to %s", fromTopic, toTopic);}

        Properties streamsConfiguration = getStreamProperties("copy");

        StreamsBuilder streamsBuilder = new StreamsBuilder();
        final KStream<String, SpecificRecord> stream = streamsBuilder.stream(fromTopic);
        stream.to(toTopic);

        return startStream(streamsBuilder, streamsConfiguration);
    }

    @Bean
    public Topology cvvRejections() {
        String fromTopic = "test2";
        String toTopic = "cvvrejections";

        if (log.isInfoEnabled()) {log.info("cvvrejections stream started from %s to %s", fromTopic, toTopic);}

        // Create Streams Config map. use `toTopic` suffix to differentiate consumer group
        Properties streamsConfiguration = getStreamProperties(toTopic);

        // Create collection with schema registry config for Serde object creation
        final Map<String, String> serdeConfig = Collections.singletonMap(
                AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);

        // Create GenericAvroSerde for use as key `isKey=true`
        final Serde<GenericRecord> genericAvroKeySerde = new GenericAvroSerde();
        genericAvroKeySerde.configure(serdeConfig, true);

        // Create SpecificAvroSerde for use as value `isKey=false`
        final Serde<SpecificRecord> transactionRequestValueSerde = new SpecificAvroSerde<SpecificRecord>();
        transactionRequestValueSerde.configure(serdeConfig, false);

        // Init StreamsBuilder `fromTopic`
        StreamsBuilder streamsBuilder = new StreamsBuilder();
        final KStream<String, SpecificRecord> stream = streamsBuilder.stream(fromTopic);

        // Rekey with customer cardnumber, vendor location and amount
        final KStream<GenericRecord, SpecificRecord> reasonRekey = stream.map(
                (transactionId, transactionRecord) -> {
                    CardReason cardReason = new CardReason();
                    cardReason.setCardnumber(transactionRecord.get(cardinal_cardnumber).toString());
                    cardReason.setReason(transactionRecord.get(cardinal_reason).toString());

                    GenericRecord recordKey = mapObjectToRecord(cardReason);
                    return new KeyValue<GenericRecord, SpecificRecord>(recordKey, transactionRecord);
                }
        ).filter((genericRecord, specificRecord) -> {
            return genericRecord.get(2).toString().equalsIgnoreCase("CVV");
        });
        // Persist to a `detail` topic
        reasonRekey.to(toTopic + ".detail", Produced.with(genericAvroKeySerde, transactionRequestValueSerde));

        // Group by the key
        final KGroupedStream<GenericRecord, SpecificRecord> groupedStream = reasonRekey.groupByKey(Grouped.with(genericAvroKeySerde, transactionRequestValueSerde));

        // Count by key
        final KTable<Windowed<GenericRecord>, Long> countIs = groupedStream
                .windowedBy(TimeWindows.of(Duration.ofMinutes(5)).advanceBy(Duration.ofMinutes(1)))
                .count();

        // Output the stream
        // countIs.toStream().print(Printed.toSysOut());

        // Create stream
        KStream<Windowed<GenericRecord>, Long> countStream = countIs.toStream().filter((key,count) -> count > 1);

        // Convert Long (count) to String
        KStream<GenericRecord, String> countStreamToPersist = countStream.map(
                (customerLocationByWindow, longCount) -> {
                    GenericRecord key = customerLocationByWindow.key();
                    return new KeyValue<>(key, longCount.toString());
                }
        );

        // Persist to `toTopic`
        countStreamToPersist.to(toTopic, Produced.with(genericAvroKeySerde, Serdes.String()));

        return startStream(streamsBuilder, streamsConfiguration);
    }

    // spend greater than 4000
    @Bean
    public Topology spendGreaterThan() {
        String fromTopic = "transactions";
        String toTopic = "spend-greater-than";

        if (log.isInfoEnabled()) {log.info("spendGreaterThan stream started from %s to %s", fromTopic, toTopic);}

        Properties streamsConfiguration = getStreamProperties("spend");
        final Serde<String> stringSerde = Serdes.String();
        final Serde<TransactionRequest> transactionRequestSerde = new SpecificAvroSerde<TransactionRequest>();

        StreamsBuilder streamsBuilder = new StreamsBuilder();
        final KStream<String, SpecificRecord> stream = streamsBuilder.stream(fromTopic);
        final KStream<String, SpecificRecord> amountRekey = stream.map(
                (KeyValueMapper<String, SpecificRecord, KeyValue<String, SpecificRecord>>) (s, specificRecord)
                        -> new KeyValue<>(specificRecord.get(cardinal_amount).toString(), specificRecord) //this creates a kafkatopic record
        );

        amountRekey.filter((s, specificRecord) -> 3999.99 < Double.valueOf(s))
                .to(toTopic);
        return startStream(streamsBuilder, streamsConfiguration);
    }

    /*@Bean
    public void spendByZipcode() {
        String fromTopic = "transactions";
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
                    locationGroup.setLocation(transactionRecord.get(cardinal_location).toString());

                    GenericRecord recordKey = mapObjectToRecord(locationGroup);
                    return new KeyValue<GenericRecord, SpecificRecord>(recordKey, transactionRecord);
                }
        );
        zipcodeRekey.to(toTopic, Produced.with(genericAvroSerde, transactionRequestSerde));

        startStream(builder, streamsConfiguration);
    } */

    /*@Bean
    public void incorrectPin() {
        String fromTopic = "transactions";
        String toTopic = "incorrectpin";

        if (log.isInfoEnabled()) {log.info("incorrectPin stream started from %s to %s", fromTopic, toTopic);}

        Properties streamsConfiguration = getStreamProperties("incorrect");

    }*/

    /*@Bean
    public Topology spendByZipcode2() {
        String fromTopic = "transactions";
        String toTopic = "spendbyzipcode-1";

        if (log.isInfoEnabled()) {log.info("spendByZipcode stream started from {} to {}", fromTopic, toTopic);}

        // Create Streams Config map. use `toTopic` suffix to differentiate consumer group
        Properties streamsConfiguration = getStreamProperties(toTopic);

        // Create collection with schema registry config for Serde object creation
        final Map<String, String> serdeConfig = Collections.singletonMap(
                AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);

        final Serde<String> stringAvroKeySerde = Serdes.String();
        stringAvroKeySerde.configure(serdeConfig, true);

        // Create GenericAvroSerde for use as key `isKey=true`
        final Serde<GenericRecord> genericAvroKeySerde = new GenericAvroSerde();
        genericAvroKeySerde.configure(serdeConfig, true);

        // Create SpecificAvroSerde for use as value `isKey=false`
        final Serde<SpecificRecord> transactionRequestValueSerde = new SpecificAvroSerde<SpecificRecord>();
        transactionRequestValueSerde.configure(serdeConfig, false);

        // Init StreamsBuilder `fromTopic`
        StreamsBuilder streamsBuilder = new StreamsBuilder();
        final KStream<String, SpecificRecord> stream = streamsBuilder.stream(fromTopic, Consumed.with(stringAvroKeySerde, transactionRequestValueSerde));

        // Rekey with customer cardnumber and vendor location
        final KStream<GenericRecord, SpecificRecord> zipcodeRekey = stream.map(
                (transactionId, transactionRecord) -> {
                    CustomerVendorLocation customerVendorLocation = new CustomerVendorLocation();
                    customerVendorLocation.setCardnumber(transactionRecord.get(cardinal_cardnumber).toString());
                    customerVendorLocation.setLocation(transactionRecord.get(cardinal_location).toString());

                    GenericRecord recordKey = mapObjectToRecord(customerVendorLocation);
                    return new KeyValue<GenericRecord, SpecificRecord>(recordKey, transactionRecord);
                }
        );

        // Group by the key
        final KGroupedStream<GenericRecord, SpecificRecord> groupedStream = zipcodeRekey.groupByKey(Grouped.with(genericAvroKeySerde, transactionRequestValueSerde));

        final KTable<GenericRecord, String> countIs = groupedStream
                .count()
                .filter(
                        (key, count) -> {
                            if (count > 2) {
                                return true;
                            }
                            return false;
                        })
                .mapValues(v -> v.toString())
                ;

        // Persist to `toTopic`
        countIs.toStream().to(toTopic, Produced.with(genericAvroKeySerde, Serdes.String()));

        return startStream(streamsBuilder, streamsConfiguration);
    }
     */

    @Bean
    public Topology spendByZipcode() {
        String fromTopic = "transactions";
        String toTopic = "spendbyzipcode-1";

        if (log.isInfoEnabled()) {log.info("spendByZipcode stream started from {} to {}", fromTopic, toTopic);}

        // Create Streams Config map. use `toTopic` suffix to differentiate consumer group
        Properties streamsConfiguration = getStreamProperties(toTopic);

        // Create collection with schema registry config for Serde object creation
        final Map<String, String> serdeConfig = Collections.singletonMap(
                AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);

        final Serde<String> stringAvroKeySerde = Serdes.String();
        stringAvroKeySerde.configure(serdeConfig, true);

        // Create GenericAvroSerde for use as key `isKey=true`
        final Serde<GenericRecord> genericAvroKeySerde = new GenericAvroSerde();
        genericAvroKeySerde.configure(serdeConfig, true);

        // Create SpecificAvroSerde for use as value `isKey=false`
        final Serde<SpecificRecord> transactionRequestValueSerde = new SpecificAvroSerde<SpecificRecord>();
        transactionRequestValueSerde.configure(serdeConfig, false);

        // Init StreamsBuilder `fromTopic`
        StreamsBuilder streamsBuilder = new StreamsBuilder();
        final KStream<String, SpecificRecord> stream = streamsBuilder.stream(fromTopic, Consumed.with(stringAvroKeySerde, transactionRequestValueSerde));

        // Rekey with customer cardnumber and vendor location
        final KStream<GenericRecord, SpecificRecord> zipcodeRekey = stream.map(
                (transactionId, transactionRecord) -> {
                    CustomerVendorLocation customerVendorLocation = new CustomerVendorLocation();
                    customerVendorLocation.setCardnumber(transactionRecord.get(cardinal_cardnumber).toString());
                    customerVendorLocation.setLocation(transactionRecord.get(cardinal_location).toString());

                    GenericRecord recordKey = mapObjectToRecord(customerVendorLocation);
                    return new KeyValue<GenericRecord, SpecificRecord>(recordKey, transactionRecord);
                }
        );
        // Persist to a `detail` topic
        zipcodeRekey.to(toTopic + ".detail", Produced.with(genericAvroKeySerde, transactionRequestValueSerde));

        // Group by the key
        final KGroupedStream<GenericRecord, SpecificRecord> groupedStream = zipcodeRekey.groupByKey(Grouped.with(genericAvroKeySerde, transactionRequestValueSerde));

        final KTable<Windowed<GenericRecord>, Long> countIs = groupedStream
                .windowedBy(TimeWindows.of(Duration.ofMinutes(5)).advanceBy(Duration.ofMinutes(1)))
                .count();

        // Create stream
        KStream<Windowed<GenericRecord>, Long> countStream = countIs.toStream().filter((key, count) -> count > 2);

        // Convert Long (count) to String
        KTable<GenericRecord, String> countStreamToPersist = countStream.map(
                (customerLocationByWindow, longCount) -> {
                    GenericRecord key = customerLocationByWindow.key();
                    return new KeyValue<>(key, longCount.toString());
                })
                .toTable(Materialized.with(genericAvroKeySerde, Serdes.String()));

        // Persist to `toTopic`
        countStreamToPersist.toStream().to(toTopic, Produced.with(genericAvroKeySerde, Serdes.String()));

        return startStream(streamsBuilder, streamsConfiguration);
    }

    @Bean
    public Topology spendbyamountlocation() {

        String fromTopic = "transactions";
        String toTopic = "spendbyamountlocation";

        if (log.isInfoEnabled()) {
            log.info("spendbyamountlocation stream started from %s to %s", fromTopic, toTopic);
        }

        // Create Streams Config map. use `toTopic` suffix to differentiate consumer group
        Properties streamsConfiguration = getStreamProperties(toTopic);

        // Create collection with schema registry config for Serde object creation
        final Map<String, String> serdeConfig = Collections.singletonMap(
                AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);

        // Create GenericAvroSerde for use as key `isKey=true`
        final Serde<GenericRecord> genericAvroKeySerde = new GenericAvroSerde();
        genericAvroKeySerde.configure(serdeConfig, true);

        // Create SpecificAvroSerde for use as value `isKey=false`
        final Serde<SpecificRecord> transactionRequestValueSerde = new SpecificAvroSerde<SpecificRecord>();
        transactionRequestValueSerde.configure(serdeConfig, false);

        // Init StreamsBuilder `fromTopic`
        StreamsBuilder streamsBuilder = new StreamsBuilder();
        final KStream<String, SpecificRecord> stream = streamsBuilder.stream(fromTopic);

        // Rekey with customer cardnumber and vendor location and amount
        final KStream<GenericRecord, SpecificRecord> zipcodeRekey = stream.map(
                (transactionId, transactionRecord) -> {
                    CustomerAmountLocation customerAmountLocation = new CustomerAmountLocation();
                    customerAmountLocation.setCardnumber(transactionRecord.get(cardinal_cardnumber).toString());
                    customerAmountLocation.setLocation(transactionRecord.get(cardinal_location).toString());
                    customerAmountLocation.setAmount(transactionRecord.get(cardinal_amount).toString());

                    GenericRecord recordKey = mapObjectToRecord(customerAmountLocation);
                    return new KeyValue<GenericRecord, SpecificRecord>(recordKey, transactionRecord);
                }
        );
        // Persist to a `detail` topic
        zipcodeRekey.to(toTopic + ".detail", Produced.with(genericAvroKeySerde, transactionRequestValueSerde));

        // Group by the key
        final KGroupedStream<GenericRecord, SpecificRecord> groupedStream = zipcodeRekey.groupByKey(Grouped.with(genericAvroKeySerde, transactionRequestValueSerde));

        final KTable<Windowed<GenericRecord>, Long> countIs = groupedStream
                .windowedBy(TimeWindows.of(Duration.ofMinutes(5)).advanceBy(Duration.ofMinutes(1)))
                .count();

        // Count by key
        //final KTable<GenericRecord, Long> countIs = groupedStream.count();

        // Output the stream
        // countIs.toStream().print(Printed.toSysOut());

        // Create stream
        //KStream<GenericRecord, Long> countStream = countIs.toStream();
        KStream<Windowed<GenericRecord>, Long> countStream = countIs.toStream().filter((key, count) -> count >= 2);

        // Convert Long (count) to String
        //KStream<GenericRecord, String> countStreamToPersist = countStream.mapValues(v -> v.toString());
        KStream<GenericRecord, String> countStreamToPersist = countStream.map(
                (customerLocationByWindow, longCount) -> {
                    GenericRecord key = customerLocationByWindow.key();
                    return new KeyValue<>(key, longCount.toString());
                }
        );

        // Persist to `toTopic`
        countStreamToPersist.to(toTopic, Produced.with(genericAvroKeySerde, Serdes.String()));

        return startStream(streamsBuilder, streamsConfiguration);
    }

    @Bean
    public Topology differentLocations() {
        String fromTopic = "transactions";
        String toTopic = "differentlocations";

        if (log.isInfoEnabled()) {log.info("differentLocations stream started from %s to %s", fromTopic, toTopic);}

        Properties streamsConfiguration = getStreamProperties("different");

        // Create collection with schema registry config for Serde object creation
        final Map<String, String> serdeConfig = Collections.singletonMap(
                AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);

        // Create GenericAvroSerde for use as key `isKey=true`
        final Serde<GenericRecord> genericAvroKeySerde = new GenericAvroSerde();
        genericAvroKeySerde.configure(serdeConfig, true);

        // Create SpecificAvroSerde for use as value `isKey=false`
        final Serde<SpecificRecord> transactionRequestSerde = new SpecificAvroSerde<SpecificRecord>();
        transactionRequestSerde.configure(serdeConfig, false);

        // Init StreamsBuilder `fromTopic`
        StreamsBuilder streamsBuilder = new StreamsBuilder();
        final KStream<String, SpecificRecord> stream = streamsBuilder.stream(fromTopic);


        HashMap<String, HashSet<String>> cardToLocs = new HashMap<>();
        stream.filter((transactionId, transactionRecord) -> {
            String cardNum = transactionRecord.get(cardinal_cardnumber).toString();
            String loc = transactionRecord.get(cardinal_location).toString();
            HashSet<String> locs = cardToLocs.getOrDefault(cardNum, new HashSet<>());
            locs.add(loc);
            cardToLocs.put(cardNum, locs);
            if (locs.size() >= 2) {
                return true;
            }
                return false;
        })
                .map((transactionId, transactionRecord) -> {
                    CustomerVendorLocation customerVendorLocation = new CustomerVendorLocation();
                    customerVendorLocation.setCardnumber(transactionRecord.get(cardinal_cardnumber).toString());
                    customerVendorLocation.setLocation(transactionRecord.get(cardinal_location).toString());
                    GenericRecord recordKey = mapObjectToRecord(customerVendorLocation);

                    return new KeyValue<GenericRecord, SpecificRecord>(recordKey, transactionRecord);
                })
                // Group by the key
                .groupByKey(Grouped.with(genericAvroKeySerde, transactionRequestSerde))
                // Count by key
                .count()
                // Create stream and convert Long (count) to String
                .toStream()
                .mapValues(v -> v.toString())
                // Persist to the `toTopic`
                .to(toTopic, Produced.with(genericAvroKeySerde, Serdes.String()));
        ;

        return startStream(streamsBuilder, streamsConfiguration);
    }

    @Bean
    public Topology spendByZipcodeConcise() {
        String fromTopic = "transactions";
        String toTopic = "spendbyzipcode.concise";

        if (log.isInfoEnabled()) {log.info("spendByZipcodeConcise stream started from %s to %s", fromTopic, toTopic);}

        // Create Streams Config map. use `toTopic` suffix to differentiate consumer group
        Properties streamsConfiguration = getStreamProperties(toTopic);

        // Create collection with schema registry config for Serde object creation
        final Map<String, String> serdeConfig = Collections.singletonMap(
                AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);

        final Serde<String> stringAvroKeySerde = Serdes.String();
        stringAvroKeySerde.configure(serdeConfig, true);

        // Create GenericAvroSerde for use as key `isKey=true`
        final Serde<GenericRecord> genericAvroKeySerde = new GenericAvroSerde();
        genericAvroKeySerde.configure(serdeConfig, true);

        // Create SpecificAvroSerde for use as value `isKey=false`
        final Serde<SpecificRecord> transactionRequestSerde = new SpecificAvroSerde<SpecificRecord>();
        transactionRequestSerde.configure(serdeConfig, false);

        // Init StreamsBuilder `fromTopic`
        StreamsBuilder streamsBuilder = new StreamsBuilder();
        final KStream<String, SpecificRecord> stream = streamsBuilder.stream(fromTopic, Consumed.with(stringAvroKeySerde, transactionRequestSerde));;

        // Rekey with customer cardnumber and vendor location
        stream.map(
                (transactionId, transactionRecord) -> {
                    CustomerVendorLocation customerVendorLocation = new CustomerVendorLocation();
                    customerVendorLocation.setCardnumber(transactionRecord.get(cardinal_cardnumber).toString());
                    customerVendorLocation.setLocation(transactionRecord.get(cardinal_location).toString());

                    GenericRecord recordKey = mapObjectToRecord(customerVendorLocation);
                    return new KeyValue<GenericRecord, SpecificRecord>(recordKey, transactionRecord);
                })
                // Group by the key
                .groupByKey(Grouped.with(genericAvroKeySerde, transactionRequestSerde))
                // Create 5 mins window
                .windowedBy(TimeWindows.of(Duration.ofMinutes(5)).advanceBy(Duration.ofMinutes(1)))
                // Count by key
                .count()
                // to a Stream to remove Windowed map reference
                .toStream()
                // filter if count > 2 i.e. more than 2 transactions by the customer from same location
                .filter((key, count) -> count > 2)
                .map(
                        (customerLocationByWindow, longCount) -> {
                            GenericRecord key = customerLocationByWindow.key();
                            return new KeyValue<>(key, longCount.toString());
                        })
                .toTable(Materialized.with(genericAvroKeySerde, Serdes.String()))
                // Create stream and convert Long (count) to String
                .toStream()
                // Persist to the `toTopic`
                .to(toTopic, Produced.with(genericAvroKeySerde, Serdes.String()));

        return startStream(streamsBuilder, streamsConfiguration);
    }

    /*@Bean
    public void differentLocations(StreamsBuilder streamsBuilder) {
        String fromTopic = "transactions";
        String toTopic = "differentlocations";

        if (log.isInfoEnabled()) {log.info("differentLocations stream started from %s to %s", fromTopic, toTopic);}

        Properties streamsConfiguration = getStreamProperties("different");

    }*/

    public static GenericData.Record mapObjectToRecord(Object object) {
        Assert.notNull(object, "object must not be null");
        final Schema schema = ReflectData.get().getSchema(object.getClass());
        final GenericData.Record record = new GenericData.Record(schema);
        schema.getFields().forEach(r -> record.put(r.name(), PropertyAccessorFactory.forDirectFieldAccess(object).getPropertyValue(r.name())));
        return record;
    }

    public static <T> T mapRecordToObject(GenericData.Record record, T object) {
        Assert.notNull(record, "record must not be null");
        Assert.notNull(object, "object must not be null");
        final Schema schema = ReflectData.get().getSchema(object.getClass());
        Assert.isTrue(schema.getFields().equals(record.getSchema().getFields()), "Schema fields didn't match");
        record.getSchema().getFields().forEach(d -> PropertyAccessorFactory.forDirectFieldAccess(object).setPropertyValue(d.name(), record.get(d.name()) == null ? record.get(d.name()) : record.get(d.name()).toString()));
        return object;
    }
}