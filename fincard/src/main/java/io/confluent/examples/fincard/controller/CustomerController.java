package io.confluent.examples.fincard.controller;

import io.confluent.examples.fincard.Customer;
import io.confluent.examples.fincard.error.RestPreconditions;
import io.confluent.examples.fincard.model.CustomerPOJO;
import io.confluent.examples.fincard.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/fincard/customer")
public class CustomerController {
    @Autowired
    private CustomerService service;

    @GetMapping(value = "/{id}")
    public Customer findById(@PathVariable("id") String id) {
        return RestPreconditions.checkFound(service.getCustomerDetail(id));
    }

    @PostMapping
    public CustomerPOJO create(@RequestBody Customer customerRequest) {
        return RestPreconditions.checkFound(service.customer(customerRequest));
    }

}
