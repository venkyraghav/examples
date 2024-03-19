package io.confluent.examples.fincard.service;

import io.confluent.examples.fincard.Customer;
import io.confluent.examples.fincard.model.CustomerPOJO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Service
public class CustomerService {

    private Map<String, Customer> customerMap = new HashMap<>();
    private final static Logger log = LoggerFactory.getLogger(CustomerService.class);

    @Autowired
    private KafkaTemplate<String, Customer> kafkaTemplate;

    public CustomerPOJO customer(Customer customer) {
        System.out.println("Customer Request " + customer.toString());
        if (log.isDebugEnabled()) {
            log.debug("Customer Request " + customer.toString());
        }

        // generate UUID (use UUID.randomUUID()) and update customer id
        String id = UUID.randomUUID().toString();

        // Produce to Kafka
        kafkaTemplate.send("customers-topic", id, customer);

        Customer response = Customer.newBuilder()
                .setId(customer.getId())
                .setName(customer.getName())
                .setZipcode(customer.getZipcode())
                .setCvv(customer.getCvv())
                .setExpdate(customer.getExpdate())
                .setPin(customer.getPin())
                .build();

        CustomerPOJO pojo = new CustomerPOJO();
        pojo.setId(response.getId());
        pojo.setName(response.getName());
        pojo.setZipCode(response.getZipcode());
        pojo.setCvv(response.getCvv());
        pojo.setExpdate(response.getExpdate());
        pojo.setPin(response.getPin());

        return pojo;
    }

    public List<Customer> allCustomers() {
        return new ArrayList<>(customerMap.values());
    }

    public Customer getCustomerDetail(final String customerId) {
        return customerMap.get(customerId);
    }
}
