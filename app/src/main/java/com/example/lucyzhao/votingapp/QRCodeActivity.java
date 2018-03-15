package com.example.lucyzhao.votingapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;

/**
 * Credits to http://www.devexchanges.info/2016/10/reading-barcodeqr-code-using-mobile.html
 */
public class QRCodeActivity extends AppCompatActivity {
    private static final String TAG = QRCodeActivity.class.getSimpleName();

    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 1100;

    SurfaceView surfaceView;
    MovingLine lineIndicator;
    SurfaceHolder surfaceHolder;
    CameraSource cs;
    BarcodeDetector bd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode);

        surfaceView = findViewById(R.id.surface_view);
        lineIndicator = findViewById(R.id.line_indicator);

        bd = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.QR_CODE)
                .build();

        cs = new CameraSource.Builder(this, bd)
                .setAutoFocusEnabled(true)
                .build();

        // determines when the surface is ready for displaying previews
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                QRCodeActivity.this.surfaceHolder = surfaceHolder;

                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_GRANTED) {
                    startScanning();
                }
                else {
                    Log.v(TAG, "requesting permission");
                    requestPermissions(
                            new String[]{Manifest.permission.CAMERA},
                            MY_PERMISSIONS_REQUEST_CAMERA);
                }

            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                cs.stop();
            }
        });

        //determines when the detector receives a result
        bd.setProcessor(new Detector.Processor<Barcode>() {
            String qrResult = "no result";
            @Override
            public void release() {
                Log.v(TAG, "in release");
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                //todo add delay
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();
                if(barcodes.size() > 0) {
                    Log.v(TAG, "received result");
                    qrResult = barcodes.valueAt(0).rawValue;

                    Intent intent = new Intent();
                    intent.putExtra(Utils.QR_RESULT, qrResult);

                    //todo send result.cancelled as well
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                }
            }
        });
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        Log.v(TAG, "entering result");
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startScanning();
                } else {
                    Log.v("tag", "camera access permission denied");
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void startScanning() {
        try {
            Log.v(TAG, "surface view is ready!");
            cs.start(surfaceHolder);

            setUpOverlay();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setUpOverlay() {
        QRCodeOverlay overlay = findViewById(R.id.overlay);
        int length = overlay.getMargin() * 2;
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(length, length);
        lp.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        lineIndicator.setLayoutParams(lp);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cs.release();
        bd.release();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

}
