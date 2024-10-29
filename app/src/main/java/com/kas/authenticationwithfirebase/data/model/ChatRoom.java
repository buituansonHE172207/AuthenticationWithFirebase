package com.kas.authenticationwithfirebase.data.model;

import java.util.List;

public class ChatRoom {
    private String chatRoomId;
    private String chatRoomName;
    private List<String> userIds;
    private Long createdAt;
    private String lastMessage;
    private Long lastMessageTimestamp;
    private boolean isGroupChat;

    public ChatRoom(String chatRoomId, String chatRoomName, List<String> userIds, Long createdAt, String lastMessage, Long lastMessageTimestamp, boolean isGroupChat) {
        this.chatRoomId = chatRoomId;
        this.chatRoomName = chatRoomName;
        this.userIds = userIds;
        this.createdAt = createdAt;
        this.lastMessage = lastMessage;
        this.lastMessageTimestamp = lastMessageTimestamp;
        this.isGroupChat = isGroupChat;
    }

    public ChatRoom() {
    }

    public String getChatRoomId() {
        return chatRoomId;
    }

    public void setChatRoomId(String chatRoomId) {
        this.chatRoomId = chatRoomId;
    }

    public List<String> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<String> userIds) {
        this.userIds = userIds;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public Long getLastMessageTimestamp() {
        return lastMessageTimestamp;
    }

    public void setLastMessageTimestamp(Long lastMessageTimestamp) {
        this.lastMessageTimestamp = lastMessageTimestamp;
    }

    public boolean isGroupChat() {
        return isGroupChat;
    }

    public void setGroupChat(boolean groupChat) {
        isGroupChat = groupChat;
    }

    public String getChatRoomName() {
        return chatRoomName;
    }

    public void setChatRoomName(String chatRoomName) {
        this.chatRoomName = chatRoomName;
    }
}
