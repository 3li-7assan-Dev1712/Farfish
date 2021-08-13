package com.example.friendlychat.Module;

public class Message {
    private String text;
    private String name;
    private String photoUrl;
    private long timestamp;


    public Message() {
    }

    public Message(String text, String name, String photoUrl) {
        this.text = text;
        this.name = name;
        this.photoUrl = photoUrl;
    }

    public Message(String text, String name, String photoUrl, long timestamp) {
        this.text = text;
        this.name = name;
        this.photoUrl = photoUrl;
        this.timestamp = timestamp;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

}
