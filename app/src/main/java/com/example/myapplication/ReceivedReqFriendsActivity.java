package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ReceivedReqFriendsActivity extends AppCompatActivity {


    private ViewFlipper viewFlipper;

    private FirebaseAuth auth;
    private String curUserId;
    private UserFriend userFriend;
    private RecyclerView friendList;

    // 用于存储匹配的用户的列表
    List<UserBean> matchingUsers = new ArrayList<>();
    private FriendRequestListAdapter adapter;
    private ArrayList<FriendRequest> friendRequestsList;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_my_friends);
        progressBar = findViewById(R.id.progressbar);
        auth = FirebaseAuth.getInstance();
        curUserId = auth.getCurrentUser().getUid();
        initView();
        Log.e("curUserId: " ,curUserId);  // ZCk0HsH9DcTSGv3dN3iGS8ZRlvN2
    }

    private void initView() {
        viewFlipper = findViewById(R.id.view_flipper);
        viewFlipper.setDisplayedChild(0);

        friendList = findViewById(R.id.request_fridents_rl);
        friendList.setLayoutManager(new LinearLayoutManager(this));

        adapter = new FriendRequestListAdapter(matchingUsers, new FriendRequestListAdapter.OnItemClickListener() {
            @Override
            public void onAccept(UserBean request) {
                // 处理接受请求的逻辑
                progressBar.setVisibility(View.VISIBLE);
                updateStatus(request.getUserId(),curUserId,"accept");
                finish();
            }

            @Override
            public void onReject(UserBean request) {
                // 处理拒绝请求的逻辑
                updateStatus(request.getUserId(),curUserId,"reject");
                finish();
            }
        });

        friendList.setAdapter(adapter);
        // viewFriendRequest();
        getFriendsRequest();

        findViewById(R.id.request_fridents_title).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void updateStatus(String fromUserId,String toUserId,String status){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("friend_requests")
                .whereEqualTo("fromUserId", fromUserId)
                .whereEqualTo("toUserId", toUserId)
                .limit(1) // 我们只期望有一个匹配
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            if (!querySnapshot.isEmpty()) {
                                // 获取第一个（也应该是唯一一个）文档的ID
                                String documentId = querySnapshot.getDocuments().get(0).getId();
                                // 现在我们有了文档ID，可以更新这个文档
                                db.collection("friend_requests").document(documentId)
                                        .update("status", status)
                                        .addOnSuccessListener(aVoid -> Log.e("updateStatus", "Friend request accepted"))
                                        .addOnFailureListener(e -> Log.e("updateStatus", "Error accepting friend request", e));

                                updateFriendList(fromUserId,toUserId);
                            } else {
                                // 没有找到匹配的文档
                                Log.e("updateStatus", "No matching friend request found");
                                progressBar.setVisibility(View.GONE);
                            }
                        } else {
                            // 查询失败
                            progressBar.setVisibility(View.GONE);
                            Log.e("updateStatus", "Error getting friend requests", task.getException());
                        }
                    }
                });

    }

    public void updateFriendList(String fromUserId, String toUserId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // 创建一个引用两个用户文档的批处理
        WriteBatch batch = db.batch();

        // 创建两个用户的文档引用
        DocumentReference currentUserRef = db.collection("usersFriends").document(toUserId);
        DocumentReference otherUserRef = db.collection("usersFriends").document(fromUserId);

        // 在当前用户的文档中添加otherUserId到friends列表
        batch.update(currentUserRef, "friends", FieldValue.arrayUnion(fromUserId));

        // 在另一个用户的文档中添加currentUserId到friends列表
        batch.update(otherUserRef, "friends", FieldValue.arrayUnion(toUserId));

        // 提交批处理
        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    // 两个用户的friends列表都已更新
                    progressBar.setVisibility(View.GONE);
                    Log.d("updateFriendList", "Both users' friends lists have been updated.");
                } else {
                    // 处理错误
                    progressBar.setVisibility(View.GONE);
                    Log.e("updateFriendList", "Error updating friends lists", task.getException());
                }
            }
        });
    }



    private void getFriendsRequest(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("friend_requests")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            friendRequestsList = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                FriendRequest friendRequest = document.toObject(FriendRequest.class);
                                friendRequestsList.add(friendRequest);
                            }
                            // 先剔除 已经接受好友或者拒绝的
                            Iterator<FriendRequest> iterator = friendRequestsList.iterator();
                            while (iterator.hasNext()) {
                                FriendRequest friendRequest = iterator.next();
                                if ("accept".equals(friendRequest.getStatus()) || "reject".equals(friendRequest.getStatus())) {
                                    iterator.remove();
                                }
                            }
                            // 在剔除不是申请自己的
                            Iterator<FriendRequest> iterator1 = friendRequestsList.iterator();
                            while (iterator1.hasNext()) {
                                FriendRequest friendRequest = iterator1.next();
                                String toUserId = friendRequest.getToUserId();
                                if (!curUserId.equals(toUserId)) {
                                    iterator1.remove();
                                }
                            }
                            // 最后得到向自己申请的好友
                            for (FriendRequest request : friendRequestsList) {
                                String requestId = request.getFromUserId();
                                for (UserBean user : MyApplication.allUserList) {
                                    if (user.getUserId().equals(requestId)) {
                                        matchingUsers.add(user);
                                        break;
                                    }
                                }
                            }
                            adapter.notifyDataSetChanged();
                        } else {
                            Log.d("getFriendsRequest", "Error getting documents: ", task.getException());
                        }
                    }
                });

    }

    public void viewFriendRequest() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("usersFriends").document(curUserId);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        //Log.e("friendRequest", "DocumentSnapshot data: " + document.getData());
                        userFriend = document.toObject(UserFriend.class);
                        Log.d("friendRequest", "DocumentSnapshot data: " + userFriend.toString());
                        List<String> friendRequest = userFriend.getFriendRequestsReceived();
                        for (String requestId : friendRequest) {
                            for (UserBean user : MyApplication.allUserList) {
                                if (user.getUserId().equals(requestId)) {
                                    matchingUsers.add(user);
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
}
