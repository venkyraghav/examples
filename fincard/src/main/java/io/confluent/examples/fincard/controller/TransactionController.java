package io.confluent.examples.fincard.controller;

import io.confluent.examples.fincard.TransactionRequest;
import io.confluent.examples.fincard.error.RestPreconditions;
import io.confluent.examples.fincard.model.TransactionResponsePOJO;
import io.confluent.examples.fincard.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/fincard/transaction")
public class TransactionController {
    @Autowired
    private TransactionService service;

    @PostMapping
    public TransactionResponsePOJO create(@RequestBody TransactionRequest transactionRequest) {
        return RestPreconditions.checkFound(service.transaction(transactionRequest));
    }
}
