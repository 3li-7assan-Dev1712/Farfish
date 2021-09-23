package com.example.friendlychat.Module;

public class Message {
    private String text;
    private String photoUrl;
    private long timestamp;
    private String senderId;
    private String targetId;
    private String senderName, targetName, targetPhotoUrl;
    private int newMessagesCount;




    public Message() {
    }

    public Message(String text, String photoUrl, long timestamp, String senderId, String targetId, String senderName, String targetName, String targetPhotoUrl) {
        this.text = text;
        this.photoUrl = photoUrl;
        this.timestamp = timestamp;
        this.senderId = senderId;
        this.targetId  = targetId;
        this.senderName = senderName;
        this.targetName = targetName;
        this.targetPhotoUrl = targetPhotoUrl;
    }

    public Message(String text, String senderName, String photoUrl) {
        this.text = text;
        this.senderName = senderName;
        this.photoUrl = photoUrl;
    }

    public Message(String text, String senderName, String photoUrl, String senderId, long timestamp) {
        this.text = text;
        this.senderName = senderName;
        this.photoUrl = photoUrl;
        this.senderId = senderId;
        this.timestamp = timestamp;
    }

    public Message(String text, String senderName, String photoUrl, long timestamp) {
        this.text = text;
        this.senderName = senderName;
        this.photoUrl = photoUrl;
        this.timestamp = timestamp;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSenderName() {
        return senderName;
    }


    public String getPhotoUrl() {
        return photoUrl;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getTargetName() {
        return targetName;
    }

    public String getTargetPhotoUrl() {
        return targetPhotoUrl;
    }

    public void setNewMessagesCount(int newMessagesCount) {
        this.newMessagesCount = newMessagesCount;
    }

    public int getNewMessagesCount() {
        return newMessagesCount;
    }

    public String getTargetId() {
        return targetId;
    }
}
