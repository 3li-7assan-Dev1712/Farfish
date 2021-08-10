package com.example.friendlychat.Module;

public class User {
    private String userName, phoneNumber, photoUrl, userId;
    private boolean isActive;
    private long lastTimeSeen;



    public User() {
    }

    public User(String userName, String phoneNumber, String photoUrl, String userId) {
        this.userName = userName;
        this.phoneNumber = phoneNumber;
        this.photoUrl = photoUrl;
        this.userId = userId;
    }

    public User(String userName, String phoneNumber, String photoUrl, String userId, boolean isActive, long lastTimeSeen) {
        this.userName = userName;
        this.phoneNumber = phoneNumber;
        this.photoUrl = photoUrl;
        this.userId = userId;
        this.isActive = isActive;
        this.lastTimeSeen = lastTimeSeen;
    }

    public String getUserName() {
        return userName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }


    public String getPhotoUrl() {
        return photoUrl;
    }

    public String getUserId() {
        return userId;
    }

    public boolean isActive() {
        return isActive;
    }

    public long getLastTimeSeen() {
        return lastTimeSeen;
    }
}
