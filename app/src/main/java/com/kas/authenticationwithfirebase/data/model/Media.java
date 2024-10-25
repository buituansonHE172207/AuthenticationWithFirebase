package com.kas.authenticationwithfirebase.data.model;

public class Media {
    private String mediaId;
    private String messageId;
    private String url;
    private String uploadedBy;
    private long timestamp;

    public Media() {
    }

    public Media(String mediaId, String messageId, String url, String uploadedBy, long timestamp) {
        this.mediaId = mediaId;
        this.messageId = messageId;
        this.url = url;
        this.uploadedBy = uploadedBy;
        this.timestamp = timestamp;
    }

    public String getMediaId() {
        return mediaId;
    }

    public void setMediaId(String mediaId) {
        this.mediaId = mediaId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(String uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
