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

        Switch darkModeSwitch = (Switch) findViewById(R.id.NightSwitch);
        Switch locationSwitch = (Switch) findViewById(R.id.LocationSwitch);
        sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        darkModeSwitch.setChecked(sharedPreferences.getBoolean("darkModeSwitch", false));
        locationSwitch.setChecked(sharedPreferences.getBoolean("locationSwitch", false));

        darkModeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                editor.putBoolean("darkModeSwitch", b).apply();
            }
        });

        locationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean c) {
                editor.putBoolean("locationSwitch", c).apply();
            }
        });
    }
}