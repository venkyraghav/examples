package io.confluent.examples.fincard.service;

import io.confluent.examples.fincard.Customer;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CustomerService {
    private Map<String, Customer> customerMap = new HashMap<>();

    public List<Customer> allCustomers() {
        return new ArrayList<>(customerMap.values());
    }

    public Customer getCustomerDetail(final long customerId) {
        return customerMap.get(String.valueOf(customerId));
    }
}
