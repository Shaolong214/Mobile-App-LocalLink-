package com.example.myapplication;

import java.util.Date;

public class Post {
    private String userId;
    private String content;
    private double latitude;
    private double longitude;
    private boolean visibleToFriends;
    private Date timestamp;

    public Post(){};

    public Post(String userId, String content, double latitude, double longitude, boolean visibleToFriends, Date timestamp) {
        this.userId = userId;
        this.content = content;
        this.latitude = latitude;
        this.longitude = longitude;
        this.visibleToFriends = visibleToFriends;
        this.timestamp = timestamp;
    }

    // Getters and Setters

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public boolean isVisibleToFriends() {
        return visibleToFriends;
    }

    public void setVisibleToFriends(boolean visibleToFriends) {
        this.visibleToFriends = visibleToFriends;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
