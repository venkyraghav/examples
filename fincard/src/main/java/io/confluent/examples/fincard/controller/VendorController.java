package io.confluent.examples.fincard.controller;

import io.confluent.examples.fincard.Vendor;
import io.confluent.examples.fincard.error.RestPreconditions;
import io.confluent.examples.fincard.model.VendorPOJO;
import io.confluent.examples.fincard.service.VendorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/fincard/vendor")
public class VendorController {
    @Autowired
    private VendorService service;

    @GetMapping(value = "/{id}")
    public Vendor findById(@PathVariable("id") String id) {
        return RestPreconditions.checkFound(service.getVendorDetail(id));
    }

    @PostMapping
    public VendorPOJO create(@RequestBody Vendor vendorRequest) {
        return RestPreconditions.checkFound(service.vendor(vendorRequest));
    }
}
