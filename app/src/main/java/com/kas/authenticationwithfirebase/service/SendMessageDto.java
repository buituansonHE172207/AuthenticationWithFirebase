package com.kas.authenticationwithfirebase.service;

//public class SendMessageDto {
//    private String to;
//    private NotificationBody notification;
//
//    public SendMessageDto(String to, NotificationBody notification) {
//        this.to = to;
//        this.notification = notification;
//    }
//
//    public String getTo() {
//        return to;
//    }
//
//    public void setTo(String to) {
//        this.to = to;
//    }
//
//    public NotificationBody getNotification() {
//        return notification;
//    }
//
//    public void setNotification(NotificationBody notification) {
//        this.notification = notification;
//    }
//}
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

