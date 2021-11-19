package com.example.farfish.Module.dataclasses;

import android.os.Parcel;
import android.os.Parcelable;
import android.telephony.PhoneNumberUtils;
import android.util.Log;

import androidx.annotation.Nullable;


public class User implements Parcelable {
    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
    private String userName, email, phoneNumber, photoUrl;
    private String userId;
    private String status;
    private boolean isActive, isPublic;
    private long lastTimeSeen;

    protected User(Parcel in) {
        userName = in.readString();
        email = in.readString();
        phoneNumber = in.readString();
        photoUrl = in.readString();
        userId = in.readString();
        status = in.readString();
        isActive = in.readByte() != 0;
        isPublic = in.readByte() != 0;
        lastTimeSeen = in.readLong();
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
        } catch (Exception ex) {
            Log.d("TAG", "equals: exception message: " + ex.getMessage());
            return false;
        }

    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(userName);
        dest.writeString(email);
        dest.writeString(phoneNumber);
        dest.writeString(photoUrl);
        dest.writeString(userId);
        dest.writeString(status);
        dest.writeByte((byte) (isActive ? 1 : 0));
        dest.writeByte((byte) (isPublic ? 1 : 0));
        dest.writeLong(lastTimeSeen);
    }
}
