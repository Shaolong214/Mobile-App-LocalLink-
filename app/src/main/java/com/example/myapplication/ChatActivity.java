package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class ChatActivity extends AppCompatActivity {
    private Button SendButton, SendAttachmentButton;
    private EditText MessageInput;
    private RecyclerView MessageList;
    private TextView ReceiverNameDisplay;
    private String receiverID, receiverName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        receiverID = getIntent().getExtras().get("receiver_user_id").toString();
        receiverName = getIntent().getExtras().get("receiver_user_name").toString();
        InitializeComponents();

    }

    private void InitializeComponents(){
        SendButton = (Button) findViewById(R.id.btnSend);
        SendAttachmentButton = (Button) findViewById(R.id.btnSendAttachment);
        MessageInput = (EditText) findViewById(R.id.etMessageInput);
        MessageList = (RecyclerView) findViewById(R.id.lvMessageList);
        ReceiverNameDisplay = (TextView) findViewById(R.id.tvReceiverTitle);
        ReceiverNameDisplay.setText(receiverName);
        findViewById(R.id.tvReceiverTitle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}