package com.example.demo;

import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.listener.ConsumerSeekAware;
import org.springframework.stereotype.Service;

@Service
public class MyConsumer implements ConsumerSeekAware {

  private final Logger logger = LoggerFactory.getLogger(MyConsumer.class);

  @KafkaListener(id = "myConsumer2", groupId = "springboot-group-2", autoStartup = "false", 
        topics="testtopicpartsjson"
        // Solution 1: Annotation based solution
        // topicPartitions= @TopicPartition(topic="testtopicparts", 
        // partitionOffsets = {
        //   @PartitionOffset(partition = "0-9", initialOffset = "1750185792116", seekPosition="TIMESTAMP")
        // })
        )
  public void listen(ConsumerRecord record) {
    //records.forEach( record -> 
      logger.info(String.format("Consumed event from topic %s: partition %d, offset %d, timestamp %d timestampType %s : key = %-10s value = %s", 
          record.topic(), record.partition(), record.offset(), 
          record.timestamp(), record.timestampType(),
          record.key(), record.value()
          )
        //)
    );
  }

  // Solution 2: Implement ConsumerSeekAware::onPartitionsAssigned to seekToTimestamp
  @Override
  public void onPartitionsAssigned(
      Map<org.apache.kafka.common.TopicPartition, Long> assignments,
      ConsumerSeekCallback callback) {
    assignments.forEach((tp, offset) -> {
      logger.info("Initial onPartitionsAssigned: Topic {} Partition {} -> Offset {}. Seeking to timestamp", tp.topic(), tp.partition(), offset);
    }
    );
    callback.seekToTimestamp(assignments.keySet(), 1750450804000L);
    // ConsumerSeekAware.super.onPartitionsAssigned(assignments, callback);
  }

  @Override
  public void registerSeekCallback(ConsumerSeekCallback callback) {
    ConsumerSeekAware.super.registerSeekCallback(callback);
  }
}