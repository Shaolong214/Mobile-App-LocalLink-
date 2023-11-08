package com.example.myapplication;


import java.util.Date;

public class FriendRequest {
    private String fromUserId;
    private String fromUserName;
    private String toUserId;

    private String toUserName;
    private String status;
    private Date timestamp;


    public FriendRequest() {
    }


    public FriendRequest(String fromUserId, String fromUserName, String toUserId, String toUserName,String status, Date timestamp) {
        this.fromUserId = fromUserId;
        this.fromUserName = fromUserName;
        this.toUserId = toUserId;
        this.status = status;
        this.timestamp = timestamp;
        this.toUserName = toUserName;
    }

    public String getToUserName() {
        return toUserName;
    }

    public void setToUserName(String toUserName) {
        this.toUserName = toUserName;
    }

    public String getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(String fromUserId) {
        this.fromUserId = fromUserId;
    }

    public String getFromUserName() {
        return fromUserName;
    }

    public void setFromUserName(String fromUserName) {
        this.fromUserName = fromUserName;
    }

    public String getToUserId() {
        return toUserId;
    }

    public void setToUserId(String toUserId) {
        this.toUserId = toUserId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "FriendRequest{" +
                "fromUserId='" + fromUserId + '\'' +
                ", fromUserName='" + fromUserName + '\'' +
                ", toUserId='" + toUserId + '\'' +
                ", toUserName='" + toUserName + '\'' +
                ", status='" + status + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
