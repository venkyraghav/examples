package io.confluent.examples.fincard.model;

public class VendorPOJO {

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return this.id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setDeviceid(long deviceid) {
        this.deviceid = deviceid;
    }

    public long getDeviceid() {
        return this.deviceid;
    }

    private long id;
    private String name;
    private long deviceid;

}
