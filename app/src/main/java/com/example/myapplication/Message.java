package com.example.myapplication;

import com.google.firebase.Timestamp;

import java.sql.Time;

public class Message {
    public String senderID, receiverID, messageTxt, timeStamp;
    public Message(){}

    public Message(String s, String r, String m, String t){
        senderID = s;
        receiverID = r;
        messageTxt = m;
        timeStamp = t;
    }
}
