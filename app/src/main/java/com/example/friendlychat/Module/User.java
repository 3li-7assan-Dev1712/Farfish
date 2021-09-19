package com.example.friendlychat.Module;

public class User {
    private String userName, email, phoneNumber, photoUrl, userId, status;
    private boolean isActive;
    private long lastTimeSeen;


    public User() {

    }

    public User(String userName, String phoneNumber, String photoUrl, String userId, String status, boolean isActive, long lastTimeSeen) {
        this.userName = userName;
        this.phoneNumber = phoneNumber;
        this.photoUrl = photoUrl;
        this.userId = userId;
        this.status = status;
        this.isActive = isActive;
        this.lastTimeSeen = lastTimeSeen;
    }

    public String getStatus() {
        return status;
    }

    public User(String userName, String phoneNumber, String photoUrl, String userId, boolean isActive, long lastTimeSeen) {
        this.userName = userName;
        this.phoneNumber = phoneNumber;
        this.photoUrl = photoUrl;
        this.userId = userId;
        this.isActive = isActive;
        this.lastTimeSeen = lastTimeSeen;
    }

    public User(String userName, String email, String phoneNumber, String photoUrl, String userId, String status, boolean isActive, long lastTimeSeen) {
        this.userName = userName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.photoUrl = photoUrl;
        this.userId = userId;
        this.status = status;
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

    public boolean getIsActive() {
        return isActive;
    }

    public long getLastTimeSeen() {
        return lastTimeSeen;
    }

    public String getEmail() {
        return email;
    }
}
