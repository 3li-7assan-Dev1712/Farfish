package com.example.friendlychat.Module;

public class Status {
    private String uploaderName, statusImage, statusText;
    private long timestamp;
    private int seenBy;

    public Status(String uploaderName, String statusImage, long timestamp, int seenBy) {
        this.uploaderName = uploaderName;
        this.statusImage = statusImage;
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

    public int getSeenBy() {
        return seenBy;
    }
}
