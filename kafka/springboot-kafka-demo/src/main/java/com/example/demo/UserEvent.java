package com.example.demo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserEvent {
    @JsonProperty("userId")
    private String userId;

    @JsonProperty("eventType")
    private String eventType;

    public UserEvent() {}
    
    public UserEvent(String userId, String eventType) {
        this.eventType = eventType;
        this.userId = userId;
    }
    
    public String getUserId() {
        return userId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
}
