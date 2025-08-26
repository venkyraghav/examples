# Getting Started

## Running the sample

* Change <REPLACE_ME> in `src/main/resources/application.yaml` to point to Confluent Cloud cluster
* `gradle clean build`
* To run producer, `gradle bootRun --args='--producer' -PjmxPort=28082`
* To run consumer, `gradle bootRun --args='--consumer'`
* Code supports `String`, `JsonSchema`, and `Protobuf` event generation
* As Is, code generates `JsonSchema` events
* To change to `String` or `Protobuf` events, modify
  * `src/main/resources/application.yaml` (de)serializer settings
  * `src/main/java/com/example.demo/SpringBootWithKafkaApplication.java` function `public CommandLineRunner CommandLineRunnerBean()`

## Reference Documentation

For further reference, please consider the following sections:

* [Official Gradle documentation](https://docs.gradle.org)
* [Spring Boot Gradle Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/3.2.3/gradle-plugin/reference/html/)
* [Create an OCI image](https://docs.spring.io/spring-boot/docs/3.2.3/gradle-plugin/reference/html/#build-image)
* [Spring for Apache Kafka](https://docs.spring.io/spring-boot/docs/3.2.3/reference/htmlsingle/index.html#messaging.kafka)

## Additional Links

These additional references should also help you:

* [Gradle Build Scans – insights for your project's build](https://scans.gradle.com#gradle)
