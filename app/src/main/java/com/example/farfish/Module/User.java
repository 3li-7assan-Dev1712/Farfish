package com.example.farfish.Module;

import android.telephony.PhoneNumberUtils;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class User {
    private String userName, email, phoneNumber, photoUrl;
    @PrimaryKey
    private String userId;
    private String status;
    private boolean isActive, isPublic;
    private long lastTimeSeen;

    public String getStatus() {
        return status;
    }

    public User() {

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

    public boolean getIsPublic() {
        return this.isPublic;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        User user = (User) obj;
        assert user != null;

        try {
            return this.userName.equals(user.getUserName()) &&
                    this.email.equals(user.getEmail()) &&
                    PhoneNumberUtils.compare(this.phoneNumber, user.getPhoneNumber()) &&
                    this.photoUrl.equals(user.getPhotoUrl()) &&
                    this.userId.equals(user.getUserId()) &&
                    this.status.equals(user.getStatus()) &&
                    this.isActive == user.getIsActive() &&
                    this.isPublic == user.getIsPublic() &&
                    this.lastTimeSeen == user.getLastTimeSeen();
        }catch (Exception ex){
            Log.d("TAG", "equals: exception message: " + ex.getMessage());
            return false;
        }

    }
}
