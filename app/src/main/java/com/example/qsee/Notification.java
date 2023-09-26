package com.example.qsee;

public class Notification {
    private String notificationId;
    private String title;
    private String message;
    private String senderId; // The user who sent the invitation
    private String groupId;  // The group related to the invitation

    public Notification() {
        // Default constructor required for Firebase
    }

    public Notification(String notificationId, String senderId, String groupId) {
        this.notificationId = notificationId;
        this.senderId = senderId;
        this.groupId = groupId;
    }

    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
}

