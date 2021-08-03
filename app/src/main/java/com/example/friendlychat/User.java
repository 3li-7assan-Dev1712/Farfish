package com.example.friendlychat;

public class User {
    private String userName, phoneNumber, email, photoUrl;

    public User() {
    }

    public User(String userName, String phoneNumber, String email, String photoUrl) {
        this.userName = userName;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.photoUrl = photoUrl;
    }

    public String getUserName() {
        return userName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }
}
