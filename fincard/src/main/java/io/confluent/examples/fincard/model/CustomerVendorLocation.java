package io.confluent.examples.fincard.model;

public class CustomerVendorLocation {
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCardnumber() {
        return cardnumber;
    }

    public void setCardnumber(String cardnumber) {
        this.cardnumber = cardnumber;
    }

    private String location;
    private String cardnumber;
}
