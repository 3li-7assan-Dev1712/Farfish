package com.example.friendlychat.Module;

import android.graphics.Bitmap;

public class FullImageData {
    private String senderName, formattedTime;
    private Bitmap bitmap;

    public FullImageData(String senderName, String formattedTime, Bitmap bitmap) {
        this.senderName = senderName;
        this.formattedTime = formattedTime;
        this.bitmap = bitmap;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getFormattedTime() {
        return formattedTime;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }
}
