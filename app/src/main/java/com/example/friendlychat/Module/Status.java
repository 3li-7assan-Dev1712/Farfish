package com.example.friendlychat.Module;

import android.os.Parcel;
import android.os.Parcelable;

public class Status implements Parcelable {
    private String uploaderName, uploaderPhotoUrl, uploaderPhoneNumber, statusImage, statusText;
    private long timestamp;
    private int seenBy;

    public Status() {
    }

    public Status(String uploaderName, String uploaderPhoneNumber, String statusImage, String statusText, long timestamp, int seenBy) {
        this.uploaderName = uploaderName;
        this.uploaderPhoneNumber = uploaderPhoneNumber;
        this.statusImage = statusImage;
        this.statusText = statusText;
        this.timestamp = timestamp;
        this.seenBy = seenBy;
    }

    public Status(String uploaderName, String statusImage, long timestamp, int seenBy) {
        this.uploaderName = uploaderName;
        this.statusImage = statusImage;
        this.timestamp = timestamp;
        this.seenBy = seenBy;
    }

    public Status(String uploaderName, String uploaderPhotoUrl, String uploaderPhoneNumber, String statusImage, String statusText, long timestamp, int seenBy) {
        this.uploaderName = uploaderName;
        this.uploaderPhotoUrl = uploaderPhotoUrl;
        this.uploaderPhoneNumber = uploaderPhoneNumber;
        this.statusImage = statusImage;
        this.statusText = statusText;
        this.timestamp = timestamp;
        this.seenBy = seenBy;
    }

    public Status(String uploaderName, String statusImage, String statusText, long timestamp, int seenBy) {
        this.uploaderName = uploaderName;
        this.statusImage = statusImage;
        this.statusText = statusText;
        this.timestamp = timestamp;
        this.seenBy = seenBy;
    }

    protected Status(Parcel in) {
        uploaderName = in.readString();
        uploaderPhotoUrl = in.readString();
        uploaderPhoneNumber = in.readString();
        statusImage = in.readString();
        statusText = in.readString();
        timestamp = in.readLong();
        seenBy = in.readInt();
    }

    public static final Creator<Status> CREATOR = new Creator<Status>() {
        @Override
        public Status createFromParcel(Parcel in) {
            return new Status(in);
        }

        @Override
        public Status[] newArray(int size) {
            return new Status[size];
        }
    };

    public String getStatusText() {
        return statusText;
    }

    public String getUploaderName() {
        return uploaderName;
    }

    public String getStatusImage() {
        return statusImage;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getUploaderPhotoUrl() {
        return uploaderPhotoUrl;
    }

    public String getUploaderPhoneNumber() {
        return uploaderPhoneNumber;
    }

    public int getSeenBy() {
        return seenBy;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uploaderName);
        dest.writeString(uploaderPhoneNumber);
        dest.writeString(uploaderPhotoUrl);
        dest.writeString(statusImage);
        dest.writeString(statusText);
        dest.writeLong(timestamp);
        dest.writeInt(seenBy);
    }
}
