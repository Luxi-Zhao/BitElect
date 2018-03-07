package com.example.lucyzhao.votingapp;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 1100;
    private static final int TAKE_PICTURE = 23;
    TextView myTextView;
    ImageView myImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myTextView = findViewById(R.id.qr_code_txt);
        myImageView = findViewById(R.id.qr_code_img);
    }

    public void vote(View view) {
        Toast.makeText(this, "you voted", Toast.LENGTH_SHORT).show();
    }

    public void scan(View view) {
        scanQRCode();
    }

    private void scanQRCode() {
        requestCameraPermission();
    }

    /**
     * Request user's permission for using camera if they haven't given
     * it
     */
    private void requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            Log.v(TAG, "camera permission already granted");
            takePicture();
        }
        else {
            // try requesting the permission again.
            Log.v(TAG, "requesting permission");
            requestPermissions(
                    new String[]{Manifest.permission.CAMERA},
                    MY_PERMISSIONS_REQUEST_CAMERA);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        Log.v(TAG, "entering result");
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.v("tag", "trying to take picture");
                    // permission was granted, take the picture
                    takePicture();
                } else {
                    Log.v("tag", "camera access permission denied");
                }
            }
        }
    }

    private void takePicture() {
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, TAKE_PICTURE);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TAKE_PICTURE && resultCode == Activity.RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            if (photo != null)
                scanPicture(photo);
            else {
                Log.v(TAG, "photo null");
            }
        }
    }

    private void scanPicture(@NonNull Bitmap myBitmap) {

        myImageView.setImageBitmap(myBitmap);

        BarcodeDetector detector =
                new BarcodeDetector.Builder(getApplicationContext())
                        .setBarcodeFormats(Barcode.QR_CODE)
                        .build();
        if(!detector.isOperational()){
            myTextView.setText("!!!!!!!!!!Could not set up the detector!");

        }
        else {
            Frame frame = new Frame.Builder().setBitmap(myBitmap).build();

            SparseArray<Barcode> barcodes = detector.detect(frame);
            Log.v(TAG, "number of barcodes: " + barcodes.size());
            if(barcodes.size() >= 1) {
                Barcode thisCode = barcodes.valueAt(0);
                myTextView.setText(thisCode.rawValue);
            }
            else {
                myTextView.setText("Can't recognize QR Code");
            }

        }
    }
}