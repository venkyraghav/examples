# EIAP Avro Consumer

This module contains a standalone Kafka consumer for Avro payloads stored in Schema Registry using record-name subject strategy, with OAuth and TLS enabled for both Kafka brokers and Schema Registry.

It is configured for `SpecificRecord` deserialization so the consumer can handle many Avro record types from the same topic, as long as matching generated classes exist on the classpath.

## Build & Run

1. ./gradlew init
2. ./gradlew build

## Run

1. Create a local copy of `src/main/resources/consumer.properties` with real values.
2. Start the consumer use `java -jar build/libs/eiap-deserializer-0.0.1-SNAPSHOT-all.jar`

```shell
java -jar build/libs/eiap-deserializer-0.0.1-SNAPSHOT-all.jar
```

3. To override configuration properties `use -Deiap.config`

```shell
java -jar build/libs/eiap-deserializer-0.0.1-SNAPSHOT-all.jar -Deiap.config=/absolute/path/to/consumer.properties
```

4. You can also override KAFKA, SCHEMA_REGISTRY, and CONSUMER values with environment variables following docker compose style

```shell
export KAFKA_BOOTSTRAP_SERVERS=broker.example.com:9092
export KAFKA_TOPIC=eiap.events
export KAFKA_GROUP_ID=eiap-avro-consumer
export KAFKA_SASL_OAUTHBEARER_TOKEN_ENDPOINT_URL=https://oauth.example.com/token
export KAFKA_SASL_JAAS_CONFIG='org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required clientId="client" clientSecret="secret" scope="kafka.read";'
export KAFKA_SSL_TRUSTSTORE_LOCATION=/path/to/kafka.truststore.jks
export KAFKA_SSL_TRUSTSTORE_PASSWORD=secret
export SCHEMA_REGISTRY_URL=https://schemaregistry.example.com
export SCHEMA_REGISTRY_BEARER_AUTH_CLIENT_ID=client
export SCHEMA_REGISTRY_BEARER_AUTH_CLIENT_SECRET=secret
export SCHEMA_REGISTRY_BEARER_AUTH_ISSUER_ENDPOINT_URL=https://oauth.example.com/token
export SCHEMA_REGISTRY_SSL_TRUSTSTORE_LOCATION=/path/to/schema-registry.truststore.jks
export SCHEMA_REGISTRY_SSL_TRUSTSTORE_PASSWORD=secret
./gradlew run
```

## Notes

- The consumer uses `KafkaAvroDeserializer` and sets `value.subject.name.strategy` to `RecordNameStrategy`.
- The consumer now uses `SpecificRecord`, not `GenericRecord`, so every schema placed under `src/main/avro` is expected to generate a matching Avro Java class during the build.
- With hundreds of schemas, the important constraint is uniqueness of record full names. Under `RecordNameStrategy`, two schemas with the same record name and namespace are a collision. If schemas omit namespace, then the bare record name must still be unique across all `.avsc` files and in Schema Registry.
- The build now validates local `.avsc` files for duplicate full names before code generation. This is the main protection you want when adding roughly 570 schemas.
- I did not rewrite the current schema to add a namespace, because that would change its record identity in Schema Registry and could break compatibility with existing subjects.
