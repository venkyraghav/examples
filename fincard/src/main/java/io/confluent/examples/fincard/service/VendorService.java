package io.confluent.examples.fincard.service;

import io.confluent.examples.fincard.Vendor;
import org.springframework.stereotype.Service;
import io.confluent.examples.fincard.model.VendorPOJO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Service
public class VendorService {

    private Map<String, Vendor> vendorMap = new HashMap<>();
    private final static Logger log = LoggerFactory.getLogger(CustomerService.class);

    @Autowired
    private KafkaTemplate<String, Vendor> kafkaTemplate;

    public VendorPOJO vendor(Vendor vendor) {
        System.out.println("Vendor Request " + vendor.toString());
        if (log.isDebugEnabled()) {
            log.debug("Vendor Request " + vendor.toString());
        }

        // generate UUID (use UUID.randomUUID()) and update customer id
        String id = UUID.randomUUID().toString();

        // Produce to Kafka
        kafkaTemplate.send("vendors", id, vendor);

        Vendor response = Vendor.newBuilder()
                .setId(vendor.getId())
                .setName(vendor.getName())
                .setDeviceid(vendor.getDeviceid())
                .build();

        VendorPOJO pojo = new VendorPOJO();
        pojo.setId(response.getId());
        pojo.setName(response.getName());
        pojo.setDeviceid(response.getDeviceid());

        return pojo;

    }


    public List<Vendor> allVendors() {
        return new ArrayList<>(vendorMap.values());
    }

    public Vendor getVendorDetail(final String vendorId) {
        return vendorMap.get(vendorId);
    }
}
