package com.example.qsee;

import java.util.Arrays;

public class Post {
    private String userName;
    private String userProfileImageUrl;
    private String postImageUrl;
    private String caption;
    private String postTime; // This could be a timestamp or a formatted string

    // Constructor
    public Post(String userName, String userProfileImageUrl, String postImageUrl, String caption, String postTime) {
        this.userName = userName;
        this.userProfileImageUrl = userProfileImageUrl;
        this.postImageUrl = postImageUrl;
        this.caption = caption;
        this.postTime = postTime;
    }

    // Getters
    public String getUserName() {
        return userName;
    }

    public String getUserProfileImageUrl() {
        return userProfileImageUrl;
    }

    public String getPostImageUrl() {
        return postImageUrl;
    }

    public String getCaption() {
        return caption;
    }

    public String getPostTime() {
        return postTime;
    }

    // Setters
    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setUserProfileImageUrl(String userProfileImageUrl) {
        this.userProfileImageUrl = userProfileImageUrl;
    }

    public void setPostImageUrl(String postImageUrl) {
        this.postImageUrl = postImageUrl;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public void setPostTime(String postTime) {
        this.postTime = postTime;
    }
}

