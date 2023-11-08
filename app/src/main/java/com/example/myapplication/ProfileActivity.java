package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
    private TextView btnBackToHome;
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

    @Override
    protected void onStart() {
        super.onStart();
        retrieveAndDisplayUserDetails();
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
//        db.collection("images").document(currentUserId)
//                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
//                    @Override
//                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot,
//                                        @Nullable FirebaseFirestoreException e) {
//                        if (e != null) {
//                            // Handle the exception
//                            return;
//                        }
//
//                        if (documentSnapshot != null && documentSnapshot.exists()) {
//                            // Fetch profile image
//                            String myProfileImage = documentSnapshot.getString("uri");
//                            if (myProfileImage != null && !myProfileImage.isEmpty()) {
//                                Picasso.get().load(myProfileImage).placeholder(R.drawable.profile).into(userProfileImage);
//                            }
//                        }
//                    }
//                });

        db.collection("images").document(currentUserId)
                .get() // Changed to get() for a single read instead of a real-time listener
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            // Fetch profile image
                            String myProfileImage = documentSnapshot.getString("uri");
                            if (myProfileImage != null && !myProfileImage.isEmpty()) {
                                Picasso.get().load(myProfileImage).placeholder(R.drawable.profile).into(userProfileImage);

                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("ProfileActivity", "Error fetching profile image", e);
                    }
                });
    }

    private void SendUserToEditProfileActivity() {
        Intent ProfileIntent = new Intent(ProfileActivity.this, EditProfileActivity.class);
        startActivity(ProfileIntent);
    }
}

