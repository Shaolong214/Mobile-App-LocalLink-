//package com.example.myapplication;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//
//import android.annotation.SuppressLint;
//import android.os.Bundle;
//import android.provider.Settings;
//import android.widget.EditText;
//import android.widget.TextView;
//
//import com.example.myapplication.R;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.ValueEventListener;
//import com.squareup.picasso.Picasso;
//
//import de.hdodenhof.circleimageview.CircleImageView;
//
//public class ProfileActivity extends AppCompatActivity {
//    private TextView userName, userStatus, userConstantName;
//    private CircleImageView userProfileImage;
//
//    private DatabaseReference profileUserRef;
//    private FirebaseAuth mAuth;
//    private String currentUserId;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_profile);
//
//        mAuth = FirebaseAuth.getInstance();
//        currentUserId = mAuth.getCurrentUser().getUid();
//        profileUserRef = FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId);
//
//        userName = findViewById(R.id.my_user_name);
//        userConstantName = findViewById(R.id.my_name);
//        userStatus = findViewById(R.id.my_profile_status);
//        userProfileImage = findViewById(R.id.my_profile_pic);
//
//        profileUserRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                if (dataSnapshot.exists()) {
//                    String myProfileImage = dataSnapshot.child("profileimage").getValue(String.class);
//                    String myUserName = dataSnapshot.child("username").getValue(String.class);
//                    String myName = dataSnapshot.child("name").getValue(String.class);
//                    String myProfileStatus = dataSnapshot.child("status").getValue(String.class);
//
//                    if (myProfileImage != null) {
//                        Picasso.get().load(myProfileImage).placeholder(R.drawable.profile).into(userProfileImage);
//                    }
//                    userName.setText("@" + myUserName);
//                    userConstantName.setText(myName);
//                    userStatus.setText(myProfileStatus);
//                }
//            }
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//                // Handle possible errors.
//            }
//        });
//    }
//}


//// match userName and name to fire database
//// implement the back function
//// implement the edit function
//// implement the profile image function


package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.squareup.picasso.Picasso;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {
    private TextView userName, userStatus, userConstantName;
    private CircleImageView userProfileImage;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        db = FirebaseFirestore.getInstance();

        userName = findViewById(R.id.my_user_name);
        userConstantName = findViewById(R.id.my_name);
        userStatus = findViewById(R.id.my_profile_status);
        userProfileImage = findViewById(R.id.my_profile_pic);

        db.collection("users").document(currentUserId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot dataSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    // Handle error
                    // ...
                    return;
                }

                if (dataSnapshot != null && dataSnapshot.exists()) {
                    String myProfileImage = dataSnapshot.getString("profileimage");
                    String myUserName = dataSnapshot.getString("username");
                    String myName = dataSnapshot.getString("name");
                    String myProfileStatus = dataSnapshot.getString("status");

                    if (myProfileImage != null && !myProfileImage.isEmpty()) {
                        Picasso.get().load(myProfileImage).placeholder(R.drawable.profile).into(userProfileImage);
                    }
                    userName.setText("@" + myUserName);
                    userConstantName.setText(myName);
                    userStatus.setText(myProfileStatus);
                }
            }
        });
    }
}

