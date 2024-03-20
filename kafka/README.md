# Kafka Examples

## Java Client Config file

Create a config file something similar to https://developer.confluent.io/get-started/java/#configuration

## Kafka Consumer Rebalance Listener Callback

### Java Client with automatic partition assignment

client uses `KafkaConsumer.subscribe` to subscribe to topic

* If `ConsumerRebalanceListener` is set as part of `subscribe` call then changes to partition assignments are published as callbacks
  * Use `onPartitionAssigned` to load session state from an external store
  * Use `onPartitionRevoked` to persist session state in an external store and/or commit offsets

### Java Client with manual partition assignment

client uses `KafkaConsumer.assign` to get topic partition assignment

### librdkafka Client with automatic partition assignment

client uses `rd_kafka_subscribe` to subscribe to topic

* `rebalance_cb` callback gets the list of all partitions that this consumer is assigned.
  * when callback gets err `RD_KAFKA_RESP_ERR__ASSIGN_PARTITION`, consumer is expected to call `rd_kafka_incremental_assign` (COOPERATIVE) else `rd_kafka_assign` (EAGER)
    * load session state from an external store
  * when callback gets err `RD_KAFKA_RESP_ERR__REVOKE_PARTITION`, consumer is expected to call `rd_kafka_incremental_unassign` (COOPERATIVE) else `rd_kafka_unassign` (EAGER)
    * persist session state in an external store and/or commit offsets

### librdkafka Client with manual partition assignment

client uses `rd_kafka_assign` to subscribe to topic and parition(s)

### Reference

https://www.confluent.io/blog/dynamic-vs-static-kafka-consumer-rebalancing/

https://www.confluent.io/kafka-summit-san-francisco-2019/static-membership-rebalance-strategy-designed-for-the-cloud/