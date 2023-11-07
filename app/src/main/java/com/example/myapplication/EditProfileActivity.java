package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileActivity extends AppCompatActivity {
    private EditText userName, userStatus;
    private CircleImageView userProfImage;
    private Button saveProfileButton, backProfileButton;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String currentUserId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        userName = (EditText) findViewById(R.id.edit_user_name);
        userStatus = (EditText) findViewById(R.id.edit_profile_status);
        saveProfileButton = (Button) findViewById(R.id.save_profile);
        backProfileButton = (Button) findViewById(R.id.edit_back_profile);
        userProfImage = (CircleImageView) findViewById(R.id.edit_profile_pic);

        retrieveAndDisplayUserDetails();

        backProfileButton = findViewById(R.id.edit_back_profile);
        backProfileButton.setOnClickListener(view -> {
            // This will send the user back to the ProfileActivity
            Intent intent = new Intent(EditProfileActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        saveProfileButton = findViewById(R.id.save_profile);
        saveProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ValidateAccountInfo();
            }
        });

    }

    private void ValidateAccountInfo() {
        String username = userName.getText().toString();
        String userstatus = userStatus.getText().toString();

        if (TextUtils.isEmpty(username)){
            Toast.makeText(this,"Please write your username...",Toast.LENGTH_SHORT).show();
        } else if(TextUtils.isEmpty(userstatus)){
            Toast.makeText(this,"Please write your status...",Toast.LENGTH_SHORT).show();
        } else {
            UpdateAccountInfo(username, userstatus);
        }
    }

    private void UpdateAccountInfo(String username, String userstatus) {
        // Creating a new HashMap for user's updated information
        HashMap<String, Object> userMap = new HashMap<>();
        userMap.put("username", username);
        userMap.put("status", userstatus);

        // Update the user's information in the Firestore database
        db.collection("users").document(currentUserId)
                .update(userMap)
                .addOnSuccessListener(aVoid -> {
                    // Inform the user that their profile has been updated
                    Toast.makeText(EditProfileActivity.this, "Profile Info updated successfully!", Toast.LENGTH_SHORT).show();

                    // Redirect back to ProfileActivity or any other activity if necessary
                    Intent intent = new Intent(EditProfileActivity.this, ProfileActivity.class);
                    startActivity(intent);
                    finish(); // If you want to finish this activity, otherwise remove this line
                })
                .addOnFailureListener(e -> {
                    // Inform the user that there was an error updating their profile
                    Toast.makeText(EditProfileActivity.this, "Error updating profile info: " + e.getMessage(), Toast.LENGTH_LONG).show();
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
                            String myProfileStatus = documentSnapshot.getString("status");

                            userName.setText(myUserName);
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
                                Picasso.get().load(myProfileImage).placeholder(R.drawable.profile).into(userProfImage);
                            }
                        }
                    }
                });
    }
}