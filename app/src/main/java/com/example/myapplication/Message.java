package com.example.myapplication;

import com.google.firebase.Timestamp;

import java.sql.Time;

public class Message {
    public String senderID, receiverID, messageTxt, timestamp;
    public Message(){}

    public Message(String s, String r, String m, String ts){
        senderID = s;
        receiverID = r;
        messageTxt = m;
        timestamp = ts;
    }
}
