package com.example.myapplication;

import java.util.Date;

public class PostMarker {
    private String type = "post";
    private String postId;
    private String content;
    private Date time;

    private String userId;



    public PostMarker(String id, String content, Date time, String user, String post) {
        this.postId = id;
        this.content = content;
        this.time = time;
        this.userId = user;
        this.type = post;
    }

    public String getPostId() {
        return postId;
    }

    public String getContent() {
        return content;
    }

    public Date getTime() {
        return time;
    }

    public String getUserId(){
        return userId;
    }

    public String getType(){return type;}
}
