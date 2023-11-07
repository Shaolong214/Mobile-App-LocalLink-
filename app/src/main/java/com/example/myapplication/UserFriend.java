package com.example.myapplication;

import java.util.List;

public class UserFriend {
    private String avatar;
    private String email;
    private List<String> friendRequestsReceived;
    private List<String> friendRequestsSent;
    private List<String> friends;
    private String username;


    // 无参数构造函数是必须的
    public UserFriend() {}

    // 这里是所有属性的getter和setter方法
    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<String> getFriendRequestsReceived() {
        return friendRequestsReceived;
    }

    public void setFriendRequestsReceived(List<String> friendRequestsReceived) {
        this.friendRequestsReceived = friendRequestsReceived;
    }

    public List<String> getFriendRequestsSent() {
        return friendRequestsSent;
    }

    public void setFriendRequestsSent(List<String> friendRequestsSent) {
        this.friendRequestsSent = friendRequestsSent;
    }

    public List<String> getFriends() {
        return friends;
    }

    public void setFriends(List<String> friends) {
        this.friends = friends;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    // 如果需要，您可以添加toString()方法，它将有助于调试
    @Override
    public String toString() {
        return "UserFriend{" +
                "avatar='" + avatar + '\'' +
                ", email='" + email + '\'' +
                ", friendRequestsReceived=" + friendRequestsReceived +
                ", friendRequestsSent=" + friendRequestsSent +
                ", friends=" + friends +
                ", username='" + username + '\'' +
                '}';
    }
}
