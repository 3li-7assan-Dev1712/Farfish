package com.example.friendlychat.Module;

public class FullMessage {
    private Message lastMessage;
    private String targetUserName;
    private String targetUserPhotoUrl;
    private String targetUserId;

    private boolean isNew = false;
    public FullMessage() {
    }

    public FullMessage(Message lastMessage, String targetUserName, String targetUserPhotoUrl, String targetUserId) {
        this.lastMessage = lastMessage;
        this.targetUserName = targetUserName;
        this.targetUserPhotoUrl = targetUserPhotoUrl;
        this.targetUserId = targetUserId;
    }

    public FullMessage(Message lastMessage, String targetUserName, String targetUserPhotoUrl, String targetUserId, boolean isNew) {
        this.lastMessage = lastMessage;
        this.targetUserName = targetUserName;
        this.targetUserPhotoUrl = targetUserPhotoUrl;
        this.targetUserId = targetUserId;
        this.isNew = isNew;
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

    public boolean getIsNew() {
        return isNew;
    }

    public void setIsNew(boolean isNew) {
        this.isNew = isNew;
    }
}
