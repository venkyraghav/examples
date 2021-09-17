package io.confluent.examples.fincard.service;

import io.confluent.examples.fincard.TransactionRequest;
import io.confluent.examples.fincard.TransactionResponse;
import io.confluent.examples.fincard.model.TransactionResponsePOJO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import io.confluent.examples.fincard.service.CustomerService;

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
        String approvalstatus;
        // Approve or Deny the transaction (randomly)
        if (((Integer.parseInt(transaction.getCardnumber())+(Integer.parseInt(transaction.getZipcode())))%5)==0) {
            approvalstatus = "DENIED";
        }
        else {
            approvalstatus = "APPROVED";
        }

       // if (transaction.getPin() != currentCustomer.getPin()) {

        //}

        // generate UUID (use UUID.randomUUID()) and update transaction id
        String id = UUID.randomUUID().toString();

        // Produce to Kafka
        kafkaTemplate.send("transactions", id, transaction);

        // TODO update appropriate fields based on transaction type
        double balance;
        if (transaction.getType().equalsIgnoreCase("credit")) {
            balance=Double.valueOf(transaction.getAmount());
        }
        else if (transaction.getType().equalsIgnoreCase("debit")){
            balance = -(Double.valueOf(transaction.getAmount()));
        }
        else {
            balance = 0;
        }
        TransactionResponse response = TransactionResponse.newBuilder()
                .setType(transaction.getType())
                .setCardnumber(transaction.getCardnumber())
                .setAmount(Double.valueOf(transaction.getAmount()))
                .setCurrency(transaction.getCurrency())
                .setCreditlimit(Double.valueOf(0.00))
                .setCreditlimitcurrency(transaction.getCurrency())
                .setCustomername(transaction.getCustomername())
                .setDatetime("NotNow")
                .setId(id)
                .setStatus(approvalstatus)
                .setNonce(Long.valueOf(System.currentTimeMillis()))
                .setReason("No Reason")
                .setNewbalance(balance)
                .build();

        TransactionResponsePOJO pojo = new TransactionResponsePOJO();
        pojo.setType(response.getType());
        pojo.setCardnumber(response.getCardnumber());
        pojo.setAmount(response.getAmount());
        pojo.setCurrency(response.getCurrency());
        pojo.setCreditlimit(response.getCreditlimit());
        pojo.setCreditlimitcurrency(response.getCreditlimitcurrency());
        pojo.setCustomername(response.getCustomername());
        pojo.setDatetime(response.getDatetime());
        pojo.setId(response.getId());
        pojo.setStatus(response.getStatus());
        pojo.setNonce(response.getNonce());
        pojo.setReason(response.getReason());
        pojo.setNewbalance(response.getNewbalance());

        return pojo;
    }
}
