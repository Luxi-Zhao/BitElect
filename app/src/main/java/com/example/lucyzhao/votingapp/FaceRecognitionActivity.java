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
import static com.google.android.gms.vision.CameraSource.CAMERA_FACING_FRONT;
import static org.bytedeco.javacpp.opencv_core.CV_32SC1;
import static org.bytedeco.javacpp.opencv_imgcodecs.CV_LOAD_IMAGE_COLOR;
import static org.bytedeco.javacpp.opencv_imgcodecs.CV_LOAD_IMAGE_GRAYSCALE;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;

import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_face;
import org.bytedeco.javacpp.opencv_face.FaceRecognizer;
import org.bytedeco.javacpp.opencv_core.MatVector;

public class FaceRecognitionActivity extends AppCompatActivity {
    private static final String TAG = FaceRecognitionActivity.class.getSimpleName();
    SurfaceView surfaceView;
    CustomFaceDetector faceDetector;
    CameraSource cs;
    ImageView testImg;
    ImageView testImg1;
    ImageView testImg2;
    ImageView testImg3;
    ImageView testImg4;
    ImageView testImg5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_recognition);
        testImg = findViewById(R.id.test_face_img);
        testImg1 = findViewById(R.id.test_face_img1);
        testImg2 = findViewById(R.id.test_face_img2);
        testImg3 = findViewById(R.id.test_face_img3);
        testImg4 = findViewById(R.id.test_face_img4);
        testImg5 = findViewById(R.id.test_face_img5);

        surfaceView = findViewById(R.id.face_surface_view);

        faceDetector = new CustomFaceDetector(new FaceDetector.Builder(this)
                .setTrackingEnabled(true)
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
                .setFacing(CAMERA_FACING_FRONT)
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

    /**
     * Seems like we can only get 4 good pics, the 5th goes to hell
     * @param view
     */
    public void showPics(View view) {
        cs.release();

        Bitmap b = Utils.retrieveImg("0", getApplicationContext());
        if(b != null && testImg != null)
            testImg.setImageBitmap(b);
        Bitmap b1 = Utils.retrieveImg("1", getApplicationContext());
        if(b1 != null && testImg1 != null)
            testImg1.setImageBitmap(b1);
        Bitmap b2 = Utils.retrieveImg("2", getApplicationContext());
        Bitmap b3 = Utils.retrieveImg("3", getApplicationContext());
        /*Bitmap b4 = Utils.retrieveImg("face7.png", getApplicationContext());
        Bitmap b5 = Utils.retrieveImg("face8.png", getApplicationContext());*/
        testImg2.setImageBitmap(b2);
        testImg3.setImageBitmap(b3);
//        testImg4.setImageBitmap(b4);
//        testImg5.setImageBitmap(b5);
    }

    public void recognizePics(View view) {
        Log.v(TAG, "start recognizing ");
        MatVector imgs = new MatVector(Utils.NUM_IMG_FILES);
        opencv_core.Mat labels = new opencv_core.Mat(Utils.NUM_IMG_FILES, 1, CV_32SC1);
        for (int i = 0; i < Utils.NUM_IMG_FILES-1; i++) {
            String filepath = getApplicationContext().getFilesDir().getAbsolutePath() + "/1-face_" + i + ".png";
            opencv_core.Mat img = imread(filepath, CV_LOAD_IMAGE_COLOR);
            if(img != null) {
                Log.v(TAG, "img " + i + " is not null");
                imgs.put(i, img);
            }
            else {
                Log.v(TAG, "img " + i + " is null!!");
            }

        }


        String testFilePath = getFilesDir().getAbsolutePath() + "/1-face_" + (Utils.NUM_IMG_FILES-1) + ".png";
        opencv_core.Mat testImg = imread(testFilePath, CV_LOAD_IMAGE_GRAYSCALE);

        FaceRecognizer faceRecognizer = opencv_face.LBPHFaceRecognizer.create();
        faceRecognizer.train(imgs, labels);

        IntPointer label = new IntPointer(1);
        DoublePointer confidence = new DoublePointer(1);
        faceRecognizer.predict(testImg, label, confidence);
        int predictedLabel = label.get(0);
        Log.v(TAG, "predicted label is " + predictedLabel);
        Log.v(TAG, "confidence is: " + confidence.get());
    }
}
