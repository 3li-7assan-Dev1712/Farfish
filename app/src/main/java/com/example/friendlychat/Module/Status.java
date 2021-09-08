package com.example.friendlychat.Module;

public class Status {
    private String uploaderName, status;
    private long timestamp;
    private int seenBy;

    public Status(String uploaderName, String status, long timestamp, int seenBy) {
        this.uploaderName = uploaderName;
        this.status = status;
        this.timestamp = timestamp;
        this.seenBy = seenBy;
    }

    public String getUploaderName() {
        return uploaderName;
    }

    public String getStatus() {
        return status;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getSeenBy() {
        return seenBy;
    }
}
