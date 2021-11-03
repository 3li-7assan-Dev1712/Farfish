package com.example.farfish.Module;

import java.util.Date;

import omari.hamza.storyview.model.MyStory;

public class CustomStory extends MyStory {

    private String statusText;

    public CustomStory(String url, Date date, String statusText) {
        super(url, date);
        this.statusText = statusText;
    }

    public String getStatusText() {
        return statusText;
    }
}
