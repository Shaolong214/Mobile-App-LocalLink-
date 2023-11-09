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
import android.widget.Toast;
import android.Manifest;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.Arrays;
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
        getFriendList();
        // show my location on map
        myMap.addMarker(new MarkerOptions().position(sydney).title("My location"));
        myMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        currentLocation = null;
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