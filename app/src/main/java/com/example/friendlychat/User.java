package com.example.friendlychat;

public class User {
    private String userName, phoneNumber, photoUrl;

    public User() {
    }

    public User(String userName, String phoneNumber, String photoUrl) {
        this.userName = userName;
        this.phoneNumber = phoneNumber;
        this.photoUrl = photoUrl;
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
}
