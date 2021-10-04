package com.example.farfish.Module;

import android.telephony.PhoneNumberUtils;

import androidx.annotation.Nullable;

public class User {
    private String userName, email, phoneNumber, photoUrl, userId, status;
    private boolean isActive, isPublic;
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

    public User(String userName, String email, String phoneNumber, String photoUrl, String userId, String status, boolean isActive, boolean isPublic, long lastTimeSeen) {
        this.userName = userName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.photoUrl = photoUrl;
        this.userId = userId;
        this.status = status;
        this.isActive = isActive;
        this.isPublic = isPublic;
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

    public boolean getIsPublic(){ return this.isPublic; }

    public String getEmail() {
        return email;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        User user = (User) obj;
        assert user != null;
        return this.userName.equals(user.getUserName()) &&
                this.email.equals(user.getEmail()) &&
                PhoneNumberUtils.compare(this.phoneNumber, user.getPhoneNumber()) &&
                this.photoUrl.equals(user.getPhotoUrl()) &&
                this.userId.equals(user.getUserId()) &&
                this.status.equals(user.getStatus()) &&
                this.isActive == user.getIsActive() &&
                this.isPublic == user.getIsPublic() &&
                this.lastTimeSeen == user.getLastTimeSeen();
    }
}
