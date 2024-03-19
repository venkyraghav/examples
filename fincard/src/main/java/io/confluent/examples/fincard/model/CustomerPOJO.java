package io.confluent.examples.fincard.model;

public class CustomerPOJO {

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setZipCode(String zip) {
        this.zipCode = zip;
    }

    public String getZipCode() {
        return this.zipCode;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }

    public String getCvv() {
        return this.cvv;
    }

    public void setExpdate(String exp) {
        this.expdate = exp;
    }

    public String getExpdate() {
        return this.expdate;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public String getPin() {
        return pin;
    }

    private String id;
    private String name;
    private String zipCode;
    private String cvv;
    private String expdate;
    private String pin;

}
