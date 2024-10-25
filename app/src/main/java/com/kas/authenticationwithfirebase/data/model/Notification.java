package com.kas.authenticationwithfirebase.data.model;

public class Notification {
    private String notificationId;
    private String recipientId;
    private String message;
    private long timestamp;
    private String type;
    private String status;

    public Notification(String notificationId, String recipientId, String message, long timestamp, String type, String status) {
        this.notificationId = notificationId;
        this.recipientId = recipientId;
        this.message = message;
        this.timestamp = timestamp;
        this.type = type;
        this.status = status;
    }

    public Notification() {
    }

    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public String getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
