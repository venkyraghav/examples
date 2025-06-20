package com.example.demo;

import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import io.confluent.example.proto.Configuration;

@Service
public class MyProducer {

  private static final Logger logger = LoggerFactory.getLogger(MyProducer.class);
  private static final String TOPIC = "testtopicpartsjson";
  private static final String TOPIC2 = "testtopicpartsjson2";

  @Autowired
  private KafkaTemplate<String, UserEvent> kafkaTemplate;

  @Autowired
  private KafkaTemplate<String, Configuration.ConfigurationT> kafkaTemplate2;

  public void sendMessageTransactional(String userId, String eventType) {
    UserEvent userEvent = new UserEvent(userId, eventType);
    sendMessageTransactional(userEvent.getUserId(), userEvent);
  }

  public void sendMessageTransactional(String key, UserEvent value) {
    kafkaTemplate.executeInTransaction(kt -> {
      CompletableFuture<SendResult<String, UserEvent>> future = kt.send(TOPIC, key, value);

      future.whenComplete((result, ex) -> {
          if (ex != null) {
              logger.error("Exception sending message to {}", TOPIC, ex);
              throw new RuntimeException(ex);
          } else {
              logger.info(String.format("Produced event to topic %s: key = %-10s value = %s", TOPIC, key, value));    
          }
      });
      future = kt.send(TOPIC2, key, value);
      future.whenComplete((result, ex) -> {
          if (ex != null) {
              logger.error("Exception sending message to {}", TOPIC2, ex);
              throw new RuntimeException(ex);
          } else {
              logger.info(String.format("Produced event to topic %s: key = %-10s value = %s", TOPIC2, key, value));    
          }
      });
      return true;
    });
  }

  public void sendMessage(String userId, String eventType) {
    UserEvent userEvent = new UserEvent(userId, eventType);
    sendMessage(userEvent.getUserId(), userEvent);
  }

  public void sendMessage(String key, UserEvent value) {
    CompletableFuture<SendResult<String, UserEvent>> future = kafkaTemplate.send(TOPIC, key, value);
    
    future.whenComplete((result, ex) -> {
        if (ex != null) {
            logger.error("Exception sending message", ex);
        } else {
            logger.info(String.format("Produced event to topic %s: key = %-10s value = %s", TOPIC, key, value));    
        }
    });
  }

  public void sendMessage(String key, Configuration.ConfigurationT value) {
    CompletableFuture<SendResult<String, Configuration.ConfigurationT>> future = kafkaTemplate2.send(TOPIC2, key, value);
    
    future.whenComplete((result, ex) -> {
        if (ex != null) {
            logger.error("Exception sending message", ex);
        } else {
            logger.info(String.format("Produced event to topic %s: key = %-10s value = %s", TOPIC2, key, value));    
        }
    });
  }
}