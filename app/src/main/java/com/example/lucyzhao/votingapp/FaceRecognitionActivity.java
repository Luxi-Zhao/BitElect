package com.example.lucyzhao.votingapp;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.io.IOException;

import static com.google.android.gms.vision.CameraSource.CAMERA_FACING_BACK;

public class FaceRecognitionActivity extends AppCompatActivity {
    private static final String TAG = FaceRecognitionActivity.class.getSimpleName();
    SurfaceView surfaceView;
    CustomFaceDetector faceDetector;
    CameraSource cs;
    ImageView testImg;
    ImageView testImg1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_recognition);

        testImg = findViewById(R.id.test_face_img);
        testImg1 = findViewById(R.id.test_face_img1);

        surfaceView = findViewById(R.id.face_surface_view);

        faceDetector = new CustomFaceDetector(new FaceDetector.Builder(this)
                .setProminentFaceOnly(true)
                .build(), this);


        //determines when the detector receives a result
        faceDetector.setProcessor(new Detector.Processor<Face>() {
            @Override
            public void release() {
                Log.v(TAG, "in release");
            }

            @Override
            public void receiveDetections(Detector.Detections<Face> detections) {

                final SparseArray<Face> faces = detections.getDetectedItems();
                if (faces.size() > 0) {
                    Log.v(TAG, "received face result");
                    //todo draw boxes?
                    Face face = faces.get(0);

                }
            }
        });


        cs = new CameraSource.Builder(this, faceDetector)
                //.setFacing(CameraSource.CAMERA_FACING_FRONT)
                .setAutoFocusEnabled(true)
                .setFacing(CAMERA_FACING_BACK)
                .setRequestedPreviewSize(320, 240)
                .build();

        // determines when the surface is ready for displaying previews
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @SuppressLint("MissingPermission")
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                Log.v(TAG, "surface view is ready!");
                try {
                    cs.start(surfaceHolder);
                } catch (IOException e) {
                    e.printStackTrace();
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

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cs.release();
        faceDetector.release();
    }

    public void showPics(View view) {
        cs.release();
        surfaceView.setVisibility(View.GONE);
        Bitmap b = Utils.retrieveImg("face0.png", getApplicationContext());
        testImg.setImageBitmap(b);
        Bitmap b1 = Utils.retrieveImg("face1.png", getApplicationContext());
        testImg1.setImageBitmap(b1);
    }
}
