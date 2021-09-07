package io.confluent.examples.fincard.service;

import io.confluent.examples.fincard.TransactionRequest;
import io.confluent.examples.fincard.TransactionResponse;
import io.confluent.examples.fincard.model.TransactionResponsePOJO;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TransactionService {
    private final static Logger log = LoggerFactory.getLogger(TransactionService.class);

    @Autowired
    private KafkaTemplate<String, TransactionRequest> kafkaTemplate;

    public TransactionResponsePOJO transaction(TransactionRequest transaction) {
        System.out.println("Transaction Request " + transaction.toString());
        if (log.isDebugEnabled()) {
            log.debug("Transaction Request " + transaction.toString());
        }

        // TODO Approve or Deny the transaction (randomly)

        // generate UUID (use UUID.randomUUID()) and update transaction id
        String id = UUID.randomUUID().toString();

        // Produce to Kafka
        // kafkaTemplate.send(id, transaction);

        // TODO update appropriate fields based on transaction type
        TransactionResponse response = TransactionResponse.newBuilder()
                .setType(transaction.getType())
                .setCardnumber(transaction.getCardnumber())
                .setAmount(Double.valueOf(transaction.getAmount()))
                .setCurrency(transaction.getCurrency())
                .setCreditlimit(Double.valueOf(0.00))
                .setCreditlimitcurrency(transaction.getCurrency())
                .setCustomerid(Integer.valueOf(0))
                .setCustomername(transaction.getCustomername())
                .setDatetime("NotNow")
                .setId(id)
                .setStatus("APPROVED")
                .setNonce(Long.valueOf(System.currentTimeMillis()))
                .setReason("No Reason")
                .build();

        TransactionResponsePOJO pojo = new TransactionResponsePOJO();
        pojo.setType(response.getType());
        pojo.setCardnumber(response.getCardnumber());
        pojo.setAmount(response.getAmount());
        pojo.setCurrency(response.getCurrency());
        pojo.setCreditlimit(response.getCreditlimit());
        pojo.setCreditlimitcurrency(response.getCreditlimitcurrency());
        pojo.setCustomerid(response.getCustomerid());
        pojo.setCustomername(response.getCustomername());
        pojo.setDatetime(response.getDatetime());
        pojo.setId(response.getId());
        pojo.setStatus(response.getStatus());
        pojo.setNonce(response.getNonce());
        pojo.setReason(response.getReason());

        return pojo;
    }
}
