package com.example.friendlychat.Module;

import java.util.Date;

import omari.hamza.storyview.model.MyStory;

public class CustomStory extends MyStory {

    private String statusText;

    public CustomStory(String url, Date date, String description, String statusText) {
        super(url, date, description);
        this.statusText = statusText;
    }

    public CustomStory(String url, Date date, String statusText) {
        super(url, date);
        this.statusText = statusText;
    }

    public CustomStory(String url, String statusText) {
        super(url);
        this.statusText = statusText;
    }

    public String getStatusText() {
        return statusText;
    }
}
