package com.kas.authenticationwithfirebase.data.entity;

public class Friend {
    private String userId;

    public Friend() {
        // Default constructor required for calls to DataSnapshot.getValue(Friend.class)
    }

    public Friend(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
