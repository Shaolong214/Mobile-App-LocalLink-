package com.example.myapplication;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.journeyapps.barcodescanner.CaptureActivity;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class QRActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    ImageView qrCode;
    Button scanButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qractivity);

        auth = FirebaseAuth.getInstance();


        qrCode = findViewById(R.id.qrCode);
        scanButton = findViewById(R.id.scanButton);

        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanQR();
            }
        });
    }

    private void scanQR() {
        ScanOptions options = new ScanOptions();
        options.setOrientationLocked(true);
        options.setCaptureActivity(CaptureAct.class);
        scanLauncher.launch(options);
    }

    ActivityResultLauncher<ScanOptions> scanLauncher = registerForActivityResult(new ScanContract(), result -> {
        if(result.getContents()!=null){
            System.out.println("tetstettdgvfgvdbfwef");
            System.out.println(result.getContents());
            AlertDialog.Builder builder = new AlertDialog.Builder(QRActivity.this);
            builder.setTitle("Result");
            builder.setMessage(result.getContents());
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    SendToAddFriend();
                }
            }).show();

        }
    });

    private void SendToAddFriend() {
        Intent addFriendIntent = new Intent(QRActivity.this, AddFriendActivity.class);
        startActivity(addFriendIntent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        
        generateQR();
    }

    private void generateQR() {
        final String userId = auth.getCurrentUser().getUid();
        MultiFormatWriter writer = new MultiFormatWriter();
        try {
            BitMatrix matrix = writer.encode(userId, BarcodeFormat.QR_CODE, 400, 400);

            BarcodeEncoder encoder = new BarcodeEncoder();
            Bitmap bitmap = encoder.createBitmap(matrix);

            qrCode.setImageBitmap(bitmap);
        } catch (WriterException e) {
            Toast.makeText(QRActivity.this, "Error occurred.", Toast.LENGTH_SHORT).show();
        }
    }
}