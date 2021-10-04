package com.example.farfish.Module;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class FullImageData implements Parcelable {
    private String senderName, formattedTime;
    private Bitmap bitmap;

    public FullImageData(String senderName, String formattedTime, Bitmap bitmap) {
        this.senderName = senderName;
        this.formattedTime = formattedTime;
        this.bitmap = bitmap;
    }

    protected FullImageData(Parcel in) {
        senderName = in.readString();
        formattedTime = in.readString();
        bitmap = in.readParcelable(Bitmap.class.getClassLoader());
    }

    public static final Creator<FullImageData> CREATOR = new Creator<FullImageData>() {
        @Override
        public FullImageData createFromParcel(Parcel in) {
            return new FullImageData(in);
        }

        @Override
        public FullImageData[] newArray(int size) {
            return new FullImageData[size];
        }
    };

    public String getSenderName() {
        return senderName;
    }

    public String getFormattedTime() {
        return formattedTime;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(senderName);
        dest.writeString(formattedTime);
        dest.writeParcelable(bitmap, flags);
    }
}
