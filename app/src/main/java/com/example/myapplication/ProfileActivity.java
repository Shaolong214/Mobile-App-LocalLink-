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


/* above v1, below v2 */


//package com.example.myapplication;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//
//import android.os.Bundle;
//import android.widget.TextView;
//
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.firestore.DocumentSnapshot;
//import com.google.firebase.firestore.EventListener;
//import com.google.firebase.firestore.FirebaseFirestore;
//import com.google.firebase.firestore.FirebaseFirestoreException;
//import com.squareup.picasso.Picasso;
//
//import javax.annotation.Nullable;
//
//import de.hdodenhof.circleimageview.CircleImageView;
//
//public class ProfileActivity extends AppCompatActivity {
//    private TextView userName, userStatus, userConstantName;
//    private CircleImageView userProfileImage;
//
//    private FirebaseFirestore db;
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
//        db = FirebaseFirestore.getInstance();
//
//        userName = findViewById(R.id.my_user_name);
//        userConstantName = findViewById(R.id.my_name);
//        userStatus = findViewById(R.id.my_profile_status);
//        userProfileImage = findViewById(R.id.my_profile_pic);
//
//        db.collection("users").document(currentUserId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
//            @Override
//            public void onEvent(@Nullable DocumentSnapshot dataSnapshot, @Nullable FirebaseFirestoreException e) {
//                if (e != null) {
//                    // Handle error
//                    // ...
//                    return;
//                }
//
//                if (dataSnapshot != null && dataSnapshot.exists()) {
//                    String myProfileImage = dataSnapshot.getString("profileimage");
//                    String myUserName = dataSnapshot.getString("username");
//                    String myName = dataSnapshot.getString("name");
//                    String myProfileStatus = dataSnapshot.getString("status");
//
//                    if (myProfileImage != null && !myProfileImage.isEmpty()) {
//                        Picasso.get().load(myProfileImage).placeholder(R.drawable.profile).into(userProfileImage);
//                    }
//                    userName.setText("@" + myUserName);
//                    userConstantName.setText(myName);
//                    userStatus.setText(myProfileStatus);
//                }
//            }
//        });
//    }
//}

/* above v2, below v3 */

package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
    private Button btnBackToHome;
    private Button EditButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        userName = findViewById(R.id.my_user_name);
        userConstantName = findViewById(R.id.my_name);
        userStatus = findViewById(R.id.my_profile_status);
        userProfileImage = findViewById(R.id.my_profile_pic);

        retrieveAndDisplayUserDetails();

        btnBackToHome = findViewById(R.id.btnBackToHome);
        btnBackToHome.setOnClickListener(view -> {
            // This will send the user back to the MainActivity
            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            startActivity(intent);
        });

        EditButton = findViewById(R.id.edit_profile);
        EditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendUserToEditProfileActivity();
            }
        });
    }

    private void retrieveAndDisplayUserDetails() {
        // Fetch user details
        db.collection("users").document(currentUserId)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            // Handle the exception
                            return;
                        }

                        if (documentSnapshot != null && documentSnapshot.exists()) {
                            // Fetch username, name, and status
                            String myUserName = documentSnapshot.getString("username");
                            String myName = documentSnapshot.getString("name");
                            String myProfileStatus = documentSnapshot.getString("status");

                            userName.setText(myUserName);
                            userConstantName.setText("@" + myName);
                            userStatus.setText(myProfileStatus);
                        }
                    }
                });

        // Fetch profile image
        db.collection("images").document(currentUserId)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            // Handle the exception
                            return;
                        }

                        if (documentSnapshot != null && documentSnapshot.exists()) {
                            // Fetch profile image
                            String myProfileImage = documentSnapshot.getString("uri");
                            if (myProfileImage != null && !myProfileImage.isEmpty()) {
                                Picasso.get().load(myProfileImage).placeholder(R.drawable.profile).into(userProfileImage);
                            }
                        }
                    }
                });
    }

    private void SendUserToEditProfileActivity() {
        Intent ProfileIntent = new Intent(ProfileActivity.this, EditProfileActivity.class);
        startActivity(ProfileIntent);
    }
}

// implement status
// implement edit
