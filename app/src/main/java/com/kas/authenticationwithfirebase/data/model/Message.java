package com.kas.authenticationwithfirebase.data.model;

import java.sql.Timestamp;
import java.util.List;

public class Message {
    private String messageId;
    private String chatRoomId;
    private String messageContent;
    private String messageType;
    private long timestamp;
    private List<String> readBy;

    public Message(String messageId, String chatRoomId, String messageContent, String messageType, long timestamp, List<String> readBy) {
        this.messageId = messageId;
        this.chatRoomId = chatRoomId;
        this.messageContent = messageContent;
        this.messageType = messageType;
        this.timestamp = timestamp;
        this.readBy = readBy;
    }

    public Message() {
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getChatRoomId() {
        return chatRoomId;
    }

    public void setChatRoomId(String chatRoomId) {
        this.chatRoomId = chatRoomId;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public List<String> getReadBy() {
        return readBy;
    }

    public void setReadBy(List<String> readBy) {
        this.readBy = readBy;
    }
}
