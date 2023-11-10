package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.contract.ActivityResultContracts;
import android.app.Activity;

public class EditProfileActivity extends AppCompatActivity {
    private EditText userName, userStatus, userCountry, userDOB, userGender;
    private CircleImageView userProfImage;
    private Button saveProfileButton, backProfileButton;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private DocumentReference imageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();


        userName = (EditText) findViewById(R.id.edit_user_name);
        userStatus = (EditText) findViewById(R.id.edit_profile_status);
        userCountry = (EditText) findViewById(R.id.edit_profile_country);
        userDOB = (EditText) findViewById(R.id.edit_profile_dob);
        userGender = (EditText) findViewById(R.id.edit_profile_gender);

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

        imageRef = db.collection("images").document(currentUserId);

        userProfImage.setOnClickListener(view -> {
            Intent galleryIntent = new Intent();
            galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
            galleryIntent.setType("image/*");
            resultLauncher.launch(galleryIntent);
        });

        imageRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@androidx.annotation.Nullable DocumentSnapshot value, @androidx.annotation.Nullable FirebaseFirestoreException error) {
                if(value!=null){
                    if(value.getData()!=null) {
                        String image = value.getData().get("uri").toString();
                        Picasso.get().load(image).placeholder(R.drawable.profile).into(userProfImage);
                    }
                }
            }
        });

    }

    ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData()!=null) {
                        Intent data = result.getData();
                        String imageUri = data.getData().toString();

                        HashMap userImage = new HashMap();
                        userImage.put("uri", imageUri);

                        imageRef.set(userImage).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Intent selfIntent = new Intent(EditProfileActivity.this, EditProfileActivity.class);
                                    startActivity(selfIntent);
                                } else {
                                    Toast.makeText(EditProfileActivity.this, "Error adding photo", Toast.LENGTH_SHORT).show();

                                }
                            }
                        });
                    }
                }
            });





    private void ValidateAccountInfo() {
        String username = userName.getText().toString();
        String userstatus = userStatus.getText().toString();
        String usercountry = userCountry.getText().toString();
        String userdob = userDOB.getText().toString();
        String usergender = userGender.getText().toString();



        if (TextUtils.isEmpty(username)){
            Toast.makeText(this,"Please write your username...",Toast.LENGTH_SHORT).show();
        } else if(TextUtils.isEmpty(userstatus)){
            Toast.makeText(this,"Please write your status...",Toast.LENGTH_SHORT).show();

        } else if(TextUtils.isEmpty(usercountry)){
            Toast.makeText(this,"Please write your country...",Toast.LENGTH_SHORT).show();

        } else if(TextUtils.isEmpty(userdob)){
            Toast.makeText(this,"Please write your DOB...",Toast.LENGTH_SHORT).show();

        } else if(TextUtils.isEmpty(usergender)){
            Toast.makeText(this,"Please write your gender...",Toast.LENGTH_SHORT).show();

        } else {
            UpdateAccountInfo(username, userstatus, userdob, usercountry, usergender);
        }
    }

    private void UpdateAccountInfo(String username, String userstatus, String userdob, String usercountry, String usergender) {
        // Creating a new HashMap for user's updated information
        HashMap<String, Object> userMap = new HashMap<>();
        userMap.put("username", username);
        userMap.put("status", userstatus);
        userMap.put("country", usercountry);
        userMap.put("DOB", userdob);
        userMap.put("gender", usergender);


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
                            String myProfileCountry = documentSnapshot.getString("country");
                            String myProfileDOB = documentSnapshot.getString("DOB");
                            String myProfileGender = documentSnapshot.getString("gender");

                            userName.setText(myUserName);
                            userStatus.setText(myProfileStatus);
                            userCountry.setText(myProfileCountry);
                            userDOB.setText(myProfileDOB);
                            userGender.setText(myProfileGender);
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
//                                Picasso.get().load(myProfileImage).placeholder(R.drawable.profile).into(userProfImage);
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
                                Picasso.get().load(myProfileImage).placeholder(R.drawable.profile).into(userProfImage);
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
}