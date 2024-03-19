package io.confluent.examples.fincard;

import io.confluent.examples.fincard.model.CustomerVendorLocation;
import io.confluent.examples.fincard.streams.FinCardSpendStream;
import io.confluent.kafka.schemaregistry.testutil.MockSchemaRegistry;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.junit.Assert.*;

@SpringBootTest
class FinCardApplicationTests {
    private TopologyTestDriver testDriver;

	@InjectMocks
	FinCardSpendStream finCardSpendStream;

	private static final String SCHEMA_REGISTRY_SCOPE = FinCardApplicationTests.class.getName();
	private static final String MOCK_SCHEMA_REGISTRY_URL = "mock://" + SCHEMA_REGISTRY_SCOPE;

	private Serde<GenericRecord> genericRecordKeySerde;
	private Serde<GenericRecord> genericRecordValueSerde;
	private Serde<SpecificRecord> specificRecordKeySerde;
	private Serde<SpecificRecord> specificRecordValueSerde;

	@BeforeEach
	public void setUp() throws Exception {
        final Map<String, String> serdeConfig = Collections.singletonMap(
                AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, MOCK_SCHEMA_REGISTRY_URL);
        specificRecordKeySerde = new SpecificAvroSerde<>();
        specificRecordKeySerde.configure(serdeConfig, true);
        specificRecordValueSerde = new SpecificAvroSerde<>();
        specificRecordValueSerde.configure(serdeConfig, false);

        genericRecordKeySerde = new GenericAvroSerde();
        genericRecordKeySerde.configure(serdeConfig, true);
        genericRecordValueSerde = new GenericAvroSerde();
        genericRecordValueSerde.configure(serdeConfig, false);

        ReflectionTestUtils.setField(finCardSpendStream, "bootstrapServers", "dummy:1234");
        ReflectionTestUtils.setField(finCardSpendStream, "schemaRegistryUrl", MOCK_SCHEMA_REGISTRY_URL);
        ReflectionTestUtils.setField(finCardSpendStream, "keySerde", "org.apache.kafka.common.serialization.Serdes.StringSerde");
        ReflectionTestUtils.setField(finCardSpendStream, "valueSerde", "io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde");
        ReflectionTestUtils.setField(finCardSpendStream, "groupId", "fincard-junit");
        ReflectionTestUtils.setField(finCardSpendStream, "applicationId", "fincard-app");
    }

    private Properties buildStreamDummyConfiguration() {
		Properties props = new Properties();
		props.put(StreamsConfig.APPLICATION_ID_CONFIG, "test123");
		props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");
		props.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, MOCK_SCHEMA_REGISTRY_URL);
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

		return props;
	}

	@AfterEach
	public void tearDown() {
		testDriver.close();
		MockSchemaRegistry.dropScope(SCHEMA_REGISTRY_SCOPE);
	}

    private TopologyTestDriver setUp(String scenario) {
        Properties props = buildStreamDummyConfiguration();
        Topology topology = null;
        testDriver = null;
        if (scenario.equalsIgnoreCase("spendByZipcode")) {
            topology = finCardSpendStream.spendByZipcode();
        }
        if (scenario.equalsIgnoreCase("spendByZipcodeConcise")) {
            topology = finCardSpendStream.spendByZipcodeConcise();
        }

        if (topology != null) {
            testDriver = new TopologyTestDriver(topology, Objects.requireNonNull(props));
        }
        return testDriver;
    }

    @Test
    public void givenData_testSpendByZipcodeConcise() {
        setUp("spendByZipcodeConcise");
        assertNotNull(testDriver);

        TestInputTopic<String, SpecificRecord> inputTopic = testDriver.createInputTopic("transactions",
                Serdes.String().serializer(), specificRecordValueSerde.serializer());
        TestOutputTopic<GenericRecord, String> outputTopic = testDriver.createOutputTopic("spendbyzipcode.concise",
                genericRecordKeySerde.deserializer(), Serdes.String().deserializer());

        assertTrue(outputTopic.isEmpty());

        inputTopic.pipeInput("1", getTransaction("1234", "30092", 100.0d));
        inputTopic.pipeInput("2", getTransaction("1234", "30092", 20.0d));
        inputTopic.pipeInput("5", getTransaction("1234", "30092", 201.0d));
        inputTopic.pipeInput("6", getTransaction("1234", "30092", 202.0d));
        inputTopic.pipeInput("3", getTransaction("5678", "30041", 120.0d));
        inputTopic.pipeInput("4", getTransaction("9012", "30092", 15.0d));

        Map<GenericRecord, String> customerLocationMap = outputTopic.readKeyValuesToMap();
        assertFalse(customerLocationMap.isEmpty());
        CustomerVendorLocation c = new CustomerVendorLocation();
        c.setCardnumber("1234");
        c.setLocation("30092");
        GenericRecord r = FinCardSpendStream.mapObjectToRecord(c);
        assertTrue(customerLocationMap.containsKey(r));
        assertEquals("4", customerLocationMap.get(r));
    }

    @Test
    public void givenData_testSpendByZipcode() {
        setUp("spendByZipcode");
        assertNotNull(testDriver);

        TestInputTopic<String, SpecificRecord> inputTopic = testDriver.createInputTopic("transactions",
                Serdes.String().serializer(), specificRecordValueSerde.serializer());
        TestOutputTopic<GenericRecord, String> outputTopic = testDriver.createOutputTopic("spendbyzipcode-1",
                genericRecordKeySerde.deserializer(), Serdes.String().deserializer());

        assertTrue(outputTopic.isEmpty());

        inputTopic.pipeInput("1", getTransaction("1234", "30092", 100.0d));
        inputTopic.pipeInput("2", getTransaction("1234", "30092", 20.0d));
        inputTopic.pipeInput("5", getTransaction("1234", "30092", 201.0d));
        inputTopic.pipeInput("6", getTransaction("1234", "30092", 202.0d));
        inputTopic.pipeInput("3", getTransaction("5678", "30041", 120.0d));
        inputTopic.pipeInput("4", getTransaction("9012", "30092", 15.0d));

        Map<GenericRecord, String> customerLocationMap = outputTopic.readKeyValuesToMap();
        assertFalse(customerLocationMap.isEmpty());
        CustomerVendorLocation c = new CustomerVendorLocation();
        c.setCardnumber("1234");
        c.setLocation("30092");
        GenericRecord r = FinCardSpendStream.mapObjectToRecord(c);
        assertTrue(customerLocationMap.containsKey(r));
        assertEquals(customerLocationMap.get(r),"4");
    }

    private TransactionRequest getTransaction(String cc, String zip, double amount) {
	    TransactionRequest request = TransactionRequest.newBuilder()
                .setCardnumber(cc)
                .setAmount(amount)
                .setCurrency("USD")
                .setCustomername(cc)
                .setDatetime("456")
                .setNonce(0L)
                .setType("CREDIT")
                .setDeviceid(0L)
                .setExpdate("000000")
                .setPin("123")
                .setLocation(zip)
                .setZipcode("30092")
                .setVendorid(0L)
                .build();

	    return request;
    }

}
