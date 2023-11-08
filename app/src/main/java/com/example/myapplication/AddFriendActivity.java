package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

public class AddFriendActivity extends AppCompatActivity {
    Boolean detected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);

        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        Sensor sensorShake = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        SensorEventListener sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                if (sensorEvent!=null){
                    float x_acc = sensorEvent.values[0];
                    float y_acc = sensorEvent.values[1];
                    float z_acc = sensorEvent.values[2];

                    float sum = Math.abs(x_acc) + Math.abs(y_acc) + Math.abs(z_acc);

                    if(sum > 14){
                        if(!detected){
                            SendUserToQR();
                            detected = true;
                        }
                    }
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };

        sensorManager.registerListener(sensorEventListener, sensorShake, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void SendUserToQR() {
      /*  Intent qrIntent = new Intent(AddFriendActivity.this, QRActivity.class);
        startActivity(qrIntent);
        finish();*/
    }
}