package com.example.farfish.Module.dataclasses;

import androidx.annotation.Nullable;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class Message {
    private String text;
    private String photoUrl;
    private long timestamp;
    private String senderId;
    private String targetId;
    private String senderName, targetName, targetPhotoUrl;
    private boolean isRead;
    private int newMessagesCount = 0;


    public Message() {
    }

    public Message(String text, String photoUrl, long timestamp, String senderId, String targetId, String senderName, String targetName, String targetPhotoUrl, boolean isRead) {
        this.text = text;
        this.photoUrl = photoUrl;
        this.timestamp = timestamp;
        this.senderId = senderId;
        this.targetId = targetId;
        this.senderName = senderName;
        this.targetName = targetName;
        this.targetPhotoUrl = targetPhotoUrl;
        this.isRead = isRead;
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

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();

        result.put("text", this.text);
        result.put("photoUrl", this.photoUrl);
        result.put("timestamp", this.timestamp);
        result.put("senderId", this.senderId);
        result.put("targetId", this.targetId);
        result.put("senderName", this.senderName);
        result.put("targetName", this.targetName);
        result.put("targetPhotoUrl", this.targetPhotoUrl);
        result.put("isRead", this.isRead);

        return result;
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

    public int getNewMessagesCount() {
        return newMessagesCount;
    }

    public void setNewMessagesCount(int newMessagesCount) {
        this.newMessagesCount = newMessagesCount;
    }

    public boolean getIsRead() {
        return this.isRead;
    }

    public void setIsRead(boolean isRead) {
        this.isRead = isRead;
    }

    public String getTargetId() {
        return targetId;
    }


    // check if the tow class are the same *_-
    @Override
    public boolean equals(@Nullable Object obj) {
        Message msg = (Message) obj;
        if (msg == null) return false;
        return this.text.equals(msg.getText()) &&
                this.photoUrl.equals(msg.getPhotoUrl()) &&
                this.timestamp == msg.getTimestamp() &&
                this.senderId.equals(msg.senderId) &&
                this.targetId.equals(msg.targetId) &&
                this.senderName.equals(msg.senderName) &&
                this.targetName.equals(msg.targetName) &&
                this.targetPhotoUrl.equals(msg.targetPhotoUrl) &&
                this.isRead == msg.getIsRead() &&
                this.newMessagesCount == msg.getNewMessagesCount();
    }
}
