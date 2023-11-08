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
        Log.e("curUserId: " ,curUserId);
    }

    private void initView() {
        viewFlipper = findViewById(R.id.view_flipper);
        viewFlipper.setDisplayedChild(0);

        friendList = findViewById(R.id.request_fridents_rl);
        friendList.setLayoutManager(new LinearLayoutManager(this));

        adapter = new FriendRequestListAdapter(matchingUsers, new FriendRequestListAdapter.OnItemClickListener() {
            @Override
            public void onAccept(UserBean request) {

                progressBar.setVisibility(View.VISIBLE);
                updateStatus(request.getUserId(),curUserId,"accept");
                finish();
            }

            @Override
            public void onReject(UserBean request) {

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
                .limit(1)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            if (!querySnapshot.isEmpty()) {

                                String documentId = querySnapshot.getDocuments().get(0).getId();

                                db.collection("friend_requests").document(documentId)
                                        .update("status", status)
                                        .addOnSuccessListener(aVoid -> Log.e("updateStatus", "Friend request accepted"))
                                        .addOnFailureListener(e -> Log.e("updateStatus", "Error accepting friend request", e));

                                updateFriendList(fromUserId,toUserId);
                            } else {

                                Log.e("updateStatus", "No matching friend request found");
                                progressBar.setVisibility(View.GONE);
                            }
                        } else {

                            progressBar.setVisibility(View.GONE);
                            Log.e("updateStatus", "Error getting friend requests", task.getException());
                        }
                    }
                });

    }

    public void updateFriendList(String fromUserId, String toUserId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        WriteBatch batch = db.batch();


        DocumentReference currentUserRef = db.collection("usersFriends").document(toUserId);
        DocumentReference otherUserRef = db.collection("usersFriends").document(fromUserId);


        batch.update(currentUserRef, "friends", FieldValue.arrayUnion(fromUserId));


        batch.update(otherUserRef, "friends", FieldValue.arrayUnion(toUserId));


        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {

                    progressBar.setVisibility(View.GONE);
                    Log.d("updateFriendList", "Both users' friends lists have been updated.");
                } else {

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

                            Iterator<FriendRequest> iterator = friendRequestsList.iterator();
                            while (iterator.hasNext()) {
                                FriendRequest friendRequest = iterator.next();
                                if ("accept".equals(friendRequest.getStatus()) || "reject".equals(friendRequest.getStatus())) {
                                    iterator.remove();
                                }
                            }

                            Iterator<FriendRequest> iterator1 = friendRequestsList.iterator();
                            while (iterator1.hasNext()) {
                                FriendRequest friendRequest = iterator1.next();
                                String toUserId = friendRequest.getToUserId();
                                if (!curUserId.equals(toUserId)) {
                                    iterator1.remove();
                                }
                            }

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
