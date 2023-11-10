package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.os.Bundle;
import android.widget.TextView;
import android.content.Context;
import android.content.SharedPreferences;

public class SettingsActivity extends AppCompatActivity {

    private TextView btnBackToHome;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        btnBackToHome = findViewById(R.id.btnBackToHome);
        btnBackToHome.setOnClickListener(view -> {
            // This will send the user back to the MainActivity
            Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
            startActivity(intent);
        });

        // Switch functionality for settings page to toggle on/off night mode and location
        Switch darkModeSwitch = (Switch) findViewById(R.id.NightSwitch);
        Switch locationSwitch = (Switch) findViewById(R.id.LocationSwitch);

        sharedPreferences = getSharedPreferences("MyPreferences1", Context.MODE_PRIVATE);

        editor = sharedPreferences.edit();

        // Set page on open to restore previous setting state
        darkModeSwitch.setChecked(sharedPreferences.getBoolean("darkModeSwitch", false));
        locationSwitch.setChecked(sharedPreferences.getBoolean("locationSwitch", false));

        // Check for dark mode switch change
        darkModeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                editor.putBoolean("darkModeSwitch", b).apply();
            }
        });

        // Check for location switch change
        locationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean c) {
                editor.putBoolean("locationSwitch", c).apply();
            }
        });
    }
}