package com.example.friendlychat.Module;

public class FullMessage {
    private Message lastMessage;
    private String targetUserName;
    private String targetUserPhotoUrl;
    private String targetUserId;

    public FullMessage() {
    }

    public FullMessage(Message lastMessage, String targetUserName, String targetUserPhotoUrl, String targetUserId) {
        this.lastMessage = lastMessage;
        this.targetUserName = targetUserName;
        this.targetUserPhotoUrl = targetUserPhotoUrl;
        this.targetUserId = targetUserId;
    }

    public Message getLastMessage() {
        return lastMessage;
    }

    public String getTargetUserName() {
        return targetUserName;
    }

    public String getTargetUserPhotoUrl() {
        return targetUserPhotoUrl;
    }

    public String getTargetUserId() {
        return targetUserId;
    }
}
