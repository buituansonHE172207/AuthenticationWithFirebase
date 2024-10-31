package com.kas.authenticationwithfirebase.data.model;

import com.kas.authenticationwithfirebase.data.entity.Message;

import java.util.List;

public class MessageWithUserDetail {
    private String messageId;
    private String chatRoomId;
    private String senderId;
    private String messageContent;
    private String messageType;
    private long timestamp;
    private List<String> readBy;
    private String username;

    public MessageWithUserDetail(String messageId, String chatRoomId, String senderId, String messageContent, String messageType, long timestamp, List<String> readBy, String username) {
        this.messageId = messageId;
        this.chatRoomId = chatRoomId;
        this.senderId = senderId;
        this.messageContent = messageContent;
        this.messageType = messageType;
        this.timestamp = timestamp;
        this.readBy = readBy;
        this.username = username;
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

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
