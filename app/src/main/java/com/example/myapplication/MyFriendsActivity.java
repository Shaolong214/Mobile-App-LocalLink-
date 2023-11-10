package com.example.myapplication;

import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.dynamic.IFragmentWrapper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyFriendsActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 1;
    private RecyclerView lvFriendsList;
    private Button btnViewFriendRequests;
    private Button btnAddFriend;
    private ViewFlipper viewFlipper;
    private Button searchFriendBtn;
    private Button sendFriendBtn;
    private FirebaseAuth auth;
    private String curUserId;
    private String toUserId;
    private EditText searchUserNameEdit;
    private String editUserName;
    private EditText sendFriendName;
    private ProgressBar progressBar;
    private boolean detected;
    private List<UserBean> friendUser = new ArrayList<>();
    private FriendListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_friends);
        lvFriendsList = findViewById(R.id.lvFriendsList);
        btnViewFriendRequests = findViewById(R.id.btnViewFriendRequests);
        progressBar = findViewById(R.id.progress);
        btnAddFriend = findViewById(R.id.btnAddFriend);
        initView();
        auth = FirebaseAuth.getInstance();
        curUserId = auth.getCurrentUser().getUid();
        Log.e("curUserId: " ,curUserId);
        initFirendList();
        initQRCode();
    }

    private void initFirendList() {
        lvFriendsList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FriendListAdapter(friendUser, new FriendListAdapter.FriendClickListener() {
            @Override
            public void onFriendClick(UserBean friend) {
                Intent intent = new Intent(MyFriendsActivity.this, ChatActivity.class);
                intent.putExtra("receiver_user_id", friend.getUserId());
                intent.putExtra("receiver_user_name", friend.getUsername());
                startActivity(intent);
            }
        });
        lvFriendsList.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("usersFriends").document(curUserId);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    friendUser.clear();
                    if (document.exists()) {
                        //Log.e("friendRequest", "DocumentSnapshot data: " + document.getData());
                        UserFriend userFriend = document.toObject(UserFriend.class);
                        Log.d("friendRequest", "DocumentSnapshot data: " + userFriend.toString());
                        List<String> friends = userFriend.getFriends();
                        for (String userId:friends) {
                            for (UserBean userBean:MyApplication.allUserList) {
                                if (TextUtils.equals(userId,userBean.getUserId())) {
                                    friendUser.add(userBean);
                                    break;
                                }
                            }
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Log.d("friendRequest", "No such document");
                    }
                } else {
                    Log.d("friendRequest", "get failed with ", task.getException());
                }
            }
        });
    }

    private void initView() {
        viewFlipper = findViewById(R.id.view_flipper);
        viewFlipper.setDisplayedChild(0);

        sendFriendName = findViewById(R.id.tvSearchNameEditResult);


        btnViewFriendRequests.setOnClickListener(view -> {

            Intent intent = new Intent();
            intent.setClass(MyFriendsActivity.this, ReceivedReqFriendsActivity.class);
            startActivity(intent);
        });

        btnAddFriend.setOnClickListener(view -> {

            viewFlipper.setDisplayedChild(1);

        });

        searchFriendBtn = findViewById(R.id.btnSearchFriend);
        searchUserNameEdit = findViewById(R.id.tvSearchNameEdit);
        searchFriendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editUserName = searchUserNameEdit.getText().toString();
                if (TextUtils.isEmpty(editUserName)) {
                    Toast.makeText(MyFriendsActivity.this,"Error. Please check and try again",Toast.LENGTH_SHORT).show();
                    return;
                }
                List<UserBean> userBeans = MyApplication.allUserList;
                if (userBeans == null) {
                    Toast.makeText(MyFriendsActivity.this,"Error. Please check and try again",Toast.LENGTH_SHORT).show();
                    return;
                }
                toUserId = searchFriend(editUserName);
                if (TextUtils.isEmpty(toUserId)) {
                    viewFlipper.setDisplayedChild(3);
                    Toast.makeText(MyFriendsActivity.this,"Error. Please check and try again",Toast.LENGTH_SHORT).show();
                    return;
                }
                sendFriendName.setText(editUserName);
                viewFlipper.setDisplayedChild(2);
            }
        });

        sendFriendBtn = findViewById(R.id.btnSendFriend);
        sendFriendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(curUserId) || TextUtils.isEmpty(toUserId)) {
                    viewFlipper.setDisplayedChild(3);
                    return;
                }
                progressBar.setVisibility(View.VISIBLE);
                List<UserBean> userBeans = MyApplication.allUserList;
                String fromUserName = null;
                String toUserName = null;
                for (UserBean bean:userBeans) {
                    if (curUserId.equals(bean.getUserId())) {
                        fromUserName = bean.getUsername();
                    }
                    if (toUserId.equals(bean.getUserId())) {
                        toUserName = bean.getUsername();
                    }
                }
                sendFriendRequest(curUserId,fromUserName,toUserId,toUserName);

            }
        });

        // title
        findViewById(R.id.tvMyFriendsTitle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        findViewById(R.id.tvSearchNameTitle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewFlipper.setDisplayedChild(0);
            }
        });
        findViewById(R.id.tvSearchNameTitleResult).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewFlipper.setDisplayedChild(1);
            }
        });
        findViewById(R.id.tvSearchNameError).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewFlipper.setDisplayedChild(1);
            }
        });

        findViewById(R.id.btnTry).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewFlipper.setDisplayedChild(1);
            }
        });
    }

    private String searchFriend(String userName){
        List<UserBean> userBeans = MyApplication.allUserList;
        for (UserBean bean:userBeans) {
            if (userName.equals(bean.getUsername()) && !(curUserId.equals(bean.getUserId()))) {
                return bean.getUserId();
            }
        }
        return "";
    }

    public void sendFriendRequest(String fromUserId,String fromUserName, String toUserId,String toUserName) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference fromUserRef = db.collection("usersFriends").document(fromUserId);
        DocumentReference toUserRef = db.collection("usersFriends").document(toUserId);

        db.runTransaction(new Transaction.Function<Void>() {
            @Nullable
            @Override
            public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {

                transaction.update(fromUserRef, "friendRequestsSent", FieldValue.arrayUnion(toUserId));

                transaction.update(toUserRef, "friendRequestsReceived", FieldValue.arrayUnion(fromUserId));

               /*
                Map<String, Object> friendRequest = new HashMap<>();
                friendRequest.put("fromUserId", fromUserId);
                friendRequest.put("toUserId", toUserId);
                friendRequest.put("status", "pending");
                friendRequest.put("timestamp", FieldValue.serverTimestamp());

                db.collection("friend_requests").add(friendRequest);*/
                Log.e("runTransaction:","begin 1111");
                createRequest(fromUserId, fromUserName,toUserId,toUserName);
                return null;
            }
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.e("runTransaction:","begin 333");
                Toast.makeText(MyFriendsActivity.this,"Successfully sent friend request",Toast.LENGTH_SHORT).show();
                viewFlipper.setDisplayedChild(0);
                progressBar.setVisibility(View.GONE);
            } else {
                Log.e("runTransaction:","begin 333---000");
                Toast.makeText(MyFriendsActivity.this,"Failed to send friend request",Toast.LENGTH_SHORT).show();
                viewFlipper.setDisplayedChild(3);
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void createRequest(String fromUserId,String fromUserName,String toUserId,String toUserName) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("friend_requests")
                .whereEqualTo("fromUserId", fromUserId)
                .whereEqualTo("toUserId", toUserId)
                .limit(1)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.e("runTransaction:","begin 222");
                            List<DocumentSnapshot> documents = task.getResult().getDocuments();
                            if (documents.isEmpty()) {

                                Map<String, Object> friendRequest = new HashMap<>();
                                friendRequest.put("fromUserId", fromUserId);
                                friendRequest.put("fromUserName", fromUserName);
                                friendRequest.put("toUserId", toUserId);
                                friendRequest.put("toUserName", toUserName);
                                friendRequest.put("status", "pending");
                                friendRequest.put("timestamp", FieldValue.serverTimestamp());

                                db.collection("friend_requests").add(friendRequest);
                            } else {
                                // 找到了匹配的记录，更新它
                                DocumentSnapshot document = documents.get(0);
                                db.collection("friend_requests")
                                        .document(document.getId())
                                        .update("timestamp", FieldValue.serverTimestamp());
                            }
                        } else {
                            Log.e("runTransaction:","begin 222--000");

                        }
                    }
                });

    }

    private void initQRCode() {
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        Sensor sensorShake = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        AlertDialog.Builder builder = new AlertDialog.Builder(MyFriendsActivity.this);
        builder.setTitle("Add A Friend");
        builder.setMessage("Select Add Friend and shake your phone to display a QR Code to add friends!");
        builder.setPositiveButton("Cool!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).show();
        SensorEventListener sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                if (sensorEvent!=null){
                    float x_acc = sensorEvent.values[0];
                    float y_acc = sensorEvent.values[1];
                    float z_acc = sensorEvent.values[2];

                    float sum = Math.abs(x_acc) + Math.abs(y_acc) + Math.abs(z_acc);

                    if(sum > 14){
                        if(!detected && viewFlipper.getDisplayedChild() == 1){
                            SendUserToQR();
                            detected = true;
                        }
                    }
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };

        sensorManager.registerListener(sensorEventListener, sensorShake, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void SendUserToQR() {
        Intent qrIntent = new Intent(this, QRActivity.class);
        startActivityForResult(qrIntent,REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            detected = false;
        }
        if (resultCode == RESULT_OK){

            if (data != null) {
                String qruserId = data.getStringExtra("qr_result");

                if (TextUtils.equals(qruserId,curUserId)) {
                    /*Intent intent = new Intent();
                    intent.setClass(MyFriendsActivity.this,ReceivedReqFriendsActivity.class);
                    startActivity(intent);*/
                    viewFlipper.setDisplayedChild(0);
                    Toast.makeText(MyFriendsActivity.this,"Waiting to add friend...",Toast.LENGTH_LONG).show();
                } else {
                    String qrUserName = null;
                    List<UserBean> userBeans = MyApplication.allUserList;
                    if (userBeans == null) {
                        Toast.makeText(MyFriendsActivity.this,"Error. Please check and try again",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    for (UserBean userBean:MyApplication.allUserList) {
                        if (userBean.getUserId().equals(qruserId)) {
                            qrUserName = userBean.getUsername();
                            break;
                        }
                    }

                    if (TextUtils.isEmpty(qrUserName)) {
                        Toast.makeText(MyFriendsActivity.this,"Error. Please check and try again",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    toUserId = qruserId;
                    sendFriendName.setText(qrUserName);
                    viewFlipper.setDisplayedChild(2);
                }
            }
        }
    }
}
