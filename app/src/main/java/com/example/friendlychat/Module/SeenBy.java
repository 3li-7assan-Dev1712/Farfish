package com.example.friendlychat.Module;

public class SeenBy {
    private String userName, userProfile;

    public SeenBy() {}

    public SeenBy(String userName, String userProfile){
        this.userName = userName;
        this.userProfile = userProfile;
    }

    public String getUserName() {
        return this.userName;
    }
    public String getUserProfile() {
        return this.userProfile;
    }

}
