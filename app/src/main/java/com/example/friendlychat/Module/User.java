package com.example.friendlychat.Module;

public class User {
    private String userName, phoneNumber, photoUrl, userId;

    public User() {
    }

    public User(String userName, String phoneNumber, String photoUrl, String userId) {
        this.userName = userName;
        this.phoneNumber = phoneNumber;
        this.photoUrl = photoUrl;
        this.userId = userId;
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
}
