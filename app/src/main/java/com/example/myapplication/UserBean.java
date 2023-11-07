package com.example.myapplication;

public class UserBean {
    public String name;
    public String username;
    private String userId;

    public UserBean() {
    }

    public UserBean(String name, String username) {
        this.name = name;
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "UserBean{" +
                "name='" + name + '\'' +
                ", username='" + username + '\'' +
                ", userId='" + userId + '\'' +
                '}';
    }

    public void setId(String id) {
        this.userId = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
