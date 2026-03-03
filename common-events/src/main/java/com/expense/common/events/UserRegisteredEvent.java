package com.expense.common.events;

import java.util.UUID;

public class UserRegisteredEvent {

    private UUID userId;
    private String email;
    private String name;

    public UserRegisteredEvent() {
    }

    public UserRegisteredEvent(UUID userId, String name, String email) {
        this.userId = userId;
        this.email = email;
        this.name = name;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setName(String name) {
        this.name = name;
    }
}