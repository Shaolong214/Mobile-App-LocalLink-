package com.example.myapplication;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.squareup.picasso.Picasso;

import androidx.activity.result.ActivityResultLauncher;

import java.util.HashMap;

public class SetupProfileActivity extends AppCompatActivity {

    private EditText SetupName, SetupUsername;
    private Button SetupButton;
    private ImageView SetupImage;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private DocumentReference userRef;
    private DocumentReference imageRef;

    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_profile);

        auth = FirebaseAuth.getInstance();
        userId = auth.getCurrentUser().getUid();
        db = FirebaseFirestore.getInstance();
        userRef = db.collection("users").document(userId);
        imageRef = db.collection("images").document(userId);

        SetupName = (EditText) findViewById(R.id.setupName);
        SetupUsername = (EditText) findViewById(R.id.setupUsername);
        SetupButton = (Button) findViewById(R.id.setupButton);
        SetupImage = (ImageView) findViewById(R.id.setupImage);

        SetupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SaveAccountDetails();
            }
        });

        SetupImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                result.launch(galleryIntent);
            }
        });

        imageRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if(value!=null){
                    if(value.getData()!=null) {
                        String image = value.getData().get("uri").toString();
                        Picasso.get().load(image).placeholder(R.drawable.profile).into(SetupImage);
                    }
                }
            }
        });

    }
    ActivityResultLauncher<Intent> result = registerForActivityResult(
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
                                    Intent selfIntent = new Intent(SetupProfileActivity.this, SetupProfileActivity.class);
                                    startActivity(selfIntent);
                                } else {
                                    Toast.makeText(SetupProfileActivity.this, "Error adding photo", Toast.LENGTH_SHORT).show();

                                }
                            }
                        });
                    }
                }
            });

    private void SaveAccountDetails() {
        String name = SetupName.getText().toString();
        String username = SetupUsername.getText().toString();

        if(TextUtils.isEmpty(name)){
            Toast.makeText(this, "Please enter a name", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(username)){
            Toast.makeText(this, "Please enter a username", Toast.LENGTH_SHORT).show();
        } else {
            HashMap userDetails = new HashMap();
            userDetails.put("name", name);
            userDetails.put("username", username);

            userRef.set(userDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        SendUserToMainActivity();
                    }
                }
            });
        }
    }

    private void SendUserToMainActivity() {
        Intent setupIntent = new Intent(SetupProfileActivity.this, MainActivity.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();
    }
}