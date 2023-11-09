package com.example.myapplication;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.Manifest;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.myapplication.databinding.ActivityMainBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback{

    private ActivityMainBinding binding;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private FirebaseUser currentUser;
    private DocumentReference imageRef;

    private Button ProfileButton;

    SupportMapFragment supportMapFragment;
    FusedLocationProviderClient fusedLocationProviderClient;
    private final int FINE_PERMISSION_CODE = 1;
    private GoogleMap myMap;
    Location currentLocation;
    private Button LogoutButton, AddFriendButton;
    private DocumentReference userRef;

    private List<String> friendList;

    String userId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
            // Rest of your code that relies on a valid user
            }

        // defining button
        LogoutButton = (Button) findViewById(R.id.logoutButton);

        ProfileButton = (Button) findViewById(R.id.profileButton);

        AddFriendButton = (Button) findViewById(R.id.addFriendButton);

        LogoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                auth.signOut();
                SendUserToLogin();
            }
        });


        ProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendUserToProfileActivity();
            }
        });

        AddFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendUserToAddFriend();
            }
        });

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);


        // map
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        getLastLocation();

    }

    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_PERMISSION_CODE);
            return;
        }
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null){
                    currentLocation = location;
                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.google_map);
                    mapFragment.getMapAsync( MainActivity.this);
                } else {
                    Toast.makeText(MainActivity.this, "Failed load map", Toast.LENGTH_SHORT).show();

                }
            }
        });
    }
    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = auth.getCurrentUser();

        if(currentUser == null){
            SendUserToLogin();
//        } else {
//            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
//            builder.setTitle("Add A Friend");
//            builder.setMessage("Shake your phone to display a QR Code to add friends!");
//            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialogInterface, int i) {
//                    dialogInterface.dismiss();
//                }
//            }).show();
//            AuthenticateUserExists();
        }

        MyApplication.initUserFireBase();
    }
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        myMap = googleMap;
        LatLng sydney = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        // update location in user collection
        if (currentUser != null) {
            updateUserLocation(currentLocation.getLatitude(), currentLocation.getLongitude());
        }
        // show friends on map
        getFriendList();
        // show my location on map
        myMap.addMarker(new MarkerOptions().position(sydney).title("My location"));
        myMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        currentLocation = null;
        // show posts on map
        showPostsOnMap();
        getFriendPostList();

        myMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                // Open a dialog or activity for post creation
                showPostDialog(latLng);
            }
        });


    }

    private void showPostDialog(LatLng latLng) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Create a Post");
        final EditText input = new EditText(this);
        builder.setView(input);
        builder.setPositiveButton("Post", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Get the post content from the input field
                String postContent = input.getText().toString().trim();

                // Create a new Post object
                Post post = new Post();
                post.setUserId(userId);
                post.setContent(postContent);
                post.setLatitude(latLng.latitude);
                post.setLongitude(latLng.longitude);
                post.setVisibleToFriends(true);
                post.setTimestamp(new Date());

                // Save the post to Firestore
                savePostToFirestore(post);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private void savePostToFirestore(Post post) {
        // Access the Firestore instance
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Get a reference to the "posts" collection
        CollectionReference postsCollection = db.collection("posts");

        // Add the post to the "posts" collection

        postsCollection.add(post)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        // Post added successfully
                        String postId = documentReference.getId();
                        addUserPostIdToFirestore(post.getUserId(), postId);
                        Toast.makeText(MainActivity.this, "Post added successfully!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle errors if post addition fails
                        Toast.makeText(MainActivity.this, "Failed to add post: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        DocumentReference userRef = db.collection("users").document(userId);
    }

    private void showPostsOnMap() {
        FirebaseUser currentUser = auth.getCurrentUser();


        if (currentUser != null) {
            String currentUserId = currentUser.getUid();

            // Get a reference to the user's document in the "users" collection
            DocumentReference userRef = db.collection("users").document(currentUserId);

            userRef.get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Retrieve the "postIds" array from the user's document

                            List<String> postIds = (List<String>) documentSnapshot.get("postIds");

                            // Retrieve and show posts on the map
                            if (postIds != null && postIds.size()>0) {
                                retrieveAndShowPosts(postIds);
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Handle errors if the query fails
                        Toast.makeText(MainActivity.this, "Failed to retrieve user's post IDs: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });

        }
    }

    private void retrieveAndShowPosts(List<String> postIds) {
        // Loop through the post IDs and retrieve each post from the "posts" collection
        for (String postId : postIds) {
            DocumentReference postRef = db.collection("posts").document(postId);

            postRef.get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Retrieve post details
                            String userId = documentSnapshot.getString("userId");
                            String content = documentSnapshot.getString("content");
                            Date time = documentSnapshot.getDate("timestamp");
                            double latitude = documentSnapshot.getDouble("latitude");
                            double longitude = documentSnapshot.getDouble("longitude");
                            boolean visibleToFriends = documentSnapshot.getBoolean("visibleToFriends");

                            // Check if the post is visible to friends
                            if (visibleToFriends) {
                                LatLng postLocation = new LatLng(latitude, longitude);
                                float hue = Math.abs((userId.hashCode() % 360) + 360) % 360;

                                MarkerOptions markerOptions = new MarkerOptions()
                                        .position(postLocation)
                                        .title(content)
                                        .icon(BitmapDescriptorFactory.defaultMarker(hue));

                                // Add marker to the map
                                Marker marker = myMap.addMarker(markerOptions);

                                // Set an InfoWindow to display post content when the marker is clicked
                                marker.setTag(new PostMarker(postId, content, time, userId));
                                myMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                                    @Override
                                    public boolean onMarkerClick(Marker clickedMarker) {

                                        PostMarker postInfo = (PostMarker) clickedMarker.getTag();
                                        if (postInfo.getUserId().equals(currentUser.getUid())){
                                            showPostActionsDialog(postInfo);
                                        } else if (!postInfo.getUserId().equals(currentUser.getUid())) {
                                            viewPostContent(postInfo);
                                        }
                                        return false;
                                    }
                                });
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Handle errors if the query fails
                        Toast.makeText(MainActivity.this, "Failed to retrieve post details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void viewPostContent(PostMarker content) {
        // Implement logic to display the post content
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Post Content");
        builder.setMessage(content.getContent());
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }

    private void showPostActionsDialog(PostMarker postInfo) {
        // Implement a dialog or activity to show post actions (view, edit, delete, change visibility)
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Post Actions");
        String[] actions = {"View Content", "Delete Post", "Change Visibility"};
        builder.setItems(actions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        // View Content
                        viewPostContent(postInfo.getContent(), postInfo.getTime());
                        break;
                    case 1:
                        // Delete Post
                        deletePost(postInfo.getPostId());
                        break;
                    case 2:
                        // Change Visibility
                        changeVisibility(postInfo.getPostId());
                        break;
                }
            }
        });

        builder.show();
    }

    private void viewPostContent(String content, Date time) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(time.toString());
        builder.setMessage(content);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }
    private void deletePost(String postId) {
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            String currentUserId = currentUser.getUid();

            // Get references to the 'posts' collection and the user's document in the 'users' collection
            DocumentReference postRef = db.collection("posts").document(postId);
            DocumentReference userRef = db.collection("users").document(currentUserId);

            // Delete the post from the 'posts' collection
            postRef.delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Post deleted successfully from the 'posts' collection

                            // Remove the post ID from the 'postIds' array in the user's document
                            userRef.update("postIds", FieldValue.arrayRemove(postId))
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            // Post ID removed successfully from the 'postIds' array
                                            Toast.makeText(MainActivity.this, "post removed, please log in again", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // Handle errors if removing post ID from 'postIds' array fails
                                            Toast.makeText(MainActivity.this, "Failed to remove post ID from user document: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Handle errors if deleting post from 'posts' collection fails
                            Toast.makeText(MainActivity.this, "Failed to delete post from posts collection: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void changeVisibility(String postId) {
        // Implement logic to change the visibility of the post
        // Update the "visibleToFriends" field in the "posts" collection
        // Implement logic to display the post content
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change visability");
        builder.setMessage("Make it invisible to your friends?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                updateVisibilityInFirestore(postId, false);
                dialogInterface.dismiss();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                updateVisibilityInFirestore(postId, true);
                dialog.cancel();
            }
        });
        builder.show();
    }

    private void updateVisibilityInFirestore(String postId, boolean isVisibleToFriends) {
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            String currentUserId = currentUser.getUid();

            // Get a reference to the 'posts' collection
            DocumentReference postRef = db.collection("posts").document(postId);

            // Update the visibility status in Firestore
            postRef.update("visibleToFriends", isVisibleToFriends)
                    .addOnSuccessListener(aVoid -> {
                        // Visibility status updated successfully
                        Toast.makeText(MainActivity.this, "updated visibility status", Toast.LENGTH_SHORT).show();

                    })
                    .addOnFailureListener(e -> {
                        // Handle errors if the update fails
                        Toast.makeText(MainActivity.this, "Failed to update visibility status: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == FINE_PERMISSION_CODE){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Location per good", Toast.LENGTH_SHORT).show();
                getLastLocation();
            } else{
                Toast.makeText(this, "Location permission is denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void addUserPostIdToFirestore(String userId, String postId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("users").document(userId);

        // Update the "postIds" array field in the user's document
        userRef.update("postIds", FieldValue.arrayUnion(postId))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Post ID added to the user's document successfully
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle errors if updating the user document fails
                        Toast.makeText(MainActivity.this, "Failed to update user document: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void getFriendList() {
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            String currentUserId = currentUser.getUid();

            // Get a reference to the "usersFriends" document for the current user
            DocumentReference userFriendsDocRef = db.collection("usersFriends").document(currentUserId);

            userFriendsDocRef.get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Retrieve the "friends" array from the document
                            String friends = documentSnapshot.get("friends").toString();
                            // Now, friendList contains the list of friend user IDs for the current user
                            // You can use this list as needed.
                            String[] itemsArray = friends.substring(1, friends.length() - 1).split(", ");
                            friendList = Arrays.asList(itemsArray);
                            showFriendsOnMap(friendList);
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Handle errors if the query fails
                        Toast.makeText(MainActivity.this, "Failed to retrieve friend list: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }
    public void getFriendPostList() {
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            String currentUserId = currentUser.getUid();

            // Get a reference to the "usersFriends" document for the current user
            DocumentReference userFriendsDocRef = db.collection("usersFriends").document(currentUserId);

            userFriendsDocRef.get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Retrieve the "friends" array from the document
                            String friends = documentSnapshot.get("friends").toString();
                            // Now, friendList contains the list of friend user IDs for the current user
                            // You can use this list as needed.
                            String[] itemsArray = friends.substring(1, friends.length() - 1).split(", ");
                            friendList = Arrays.asList(itemsArray);
                            showFriendsPostOnMap(friendList);



                        }
                    })
                    .addOnFailureListener(e -> {
                        // Handle errors if the query fails
                        Toast.makeText(MainActivity.this, "Failed to retrieve friend list: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    public void showFriendsPostOnMap(List<String> friendList) {
        for (String friendUserId : friendList) {
            // Get a reference to the friend's user document in the "users" collection
            DocumentReference friendUserRef = db.collection("users").document(friendUserId);

            friendUserRef.get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Retrieve the "postIds" array from the user's document

                            List<String> postIds = (List<String>) documentSnapshot.get("postIds");

                            // Retrieve and show posts on the map
                            if (postIds != null && postIds.size() > 0) {
                                retrieveAndShowPosts(postIds);
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Handle errors if the query fails
                        Toast.makeText(MainActivity.this, "Failed to retrieve user's post IDs: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    public void showFriendsOnMap(List<String> friendList) {
        for (String friendUserId : friendList) {
            // Get a reference to the friend's user document in the "users" collection
            DocumentReference friendUserRef = db.collection("users").document(friendUserId);

            friendUserRef.get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Retrieve the latitude and longitude from the friend's document
                            Double friendLatitude = documentSnapshot.getDouble("latitude");
                            Double friendLongitude = documentSnapshot.getDouble("longitude");
                            String friendName = documentSnapshot.getString("name");

                            if (friendLatitude != null && friendLongitude != null) {
                                LatLng friendLocation = new LatLng(friendLatitude, friendLongitude);
                                // Add a marker for the friend's location on the map
                                float hue = Math.abs((friendUserId.hashCode() % 360) + 360) % 360;
                                myMap.addMarker(new MarkerOptions().position(friendLocation)
                                        .title(friendName)
                                        .icon(BitmapDescriptorFactory
                                                .defaultMarker(hue)));
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Handle errors if the query fails
                        Toast.makeText(MainActivity.this, "Failed to retrieve friend's location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }
    public void updateUserLocation(double lat, double lon){
        userId = currentUser.getUid();
        userRef = db.collection("users").document(userId);
        Map<String, Object> locationData = new HashMap<>();
        locationData.put("latitude", lat);
        locationData.put("longitude", lon);

        // Update the user's location in Firestore
        userRef.update(locationData)

                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Location data updated successfully
                        Toast.makeText(MainActivity.this, "Location updated in Firestore", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle any errors that occur during the update
                        Toast.makeText(MainActivity.this, "Failed to update location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void AuthenticateUserExists() {
        final String userId = auth.getCurrentUser().getUid();
        imageRef = db.collection("images").document(userId);
        imageRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
        @Override
        public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
            /*if(value!=null){
                if(value.getData()==null) {
                    SendUserToSetupProfileActivity();
                }
            }*/
        }
    });
    }
    private void SendUserToSetupProfileActivity() {
        Intent setupProfileIntent = new Intent(MainActivity.this, SetupProfileActivity.class);
        setupProfileIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupProfileIntent);
        finish();
    }

    private void SendUserToLogin() {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    private void SendUserToProfileActivity() {
        Intent ProfileIntent = new Intent(MainActivity.this, ProfileActivity.class);
        startActivity(ProfileIntent);
    }

    private void SendUserToAddFriend() {
        if (true) {
            Intent addFriendIntent = new Intent(MainActivity.this, MyFriendsActivity.class);
            startActivity(addFriendIntent);
            return;
        }
        Intent addFriendIntent = new Intent(MainActivity.this, AddFriendActivity.class);
        addFriendIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(addFriendIntent);
        finish();
    }

}