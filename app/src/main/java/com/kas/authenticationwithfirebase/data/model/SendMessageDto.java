package com.kas.authenticationwithfirebase.data.model;

public class SendMessageDto {
    private Message message;

    public SendMessageDto(String to, NotificationBody notification) {
        this.message = new Message();
        this.message.setToken(to);
        this.message.setNotification(notification);
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public static class Message {
        private String token;
        private NotificationBody notification;

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public NotificationBody getNotification() {
            return notification;
        }

        public void setNotification(NotificationBody notification) {
            this.notification = notification;
        }
    }
}

