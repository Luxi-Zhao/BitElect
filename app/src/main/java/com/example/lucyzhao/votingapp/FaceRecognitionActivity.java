package com.example.lucyzhao.votingapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_face;
import org.jmrtd.Util;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.IntBuffer;
import java.util.ArrayList;

import static com.example.lucyzhao.votingapp.Utils.NUM_CAPTUREDS;
import static com.example.lucyzhao.votingapp.Utils.YOUR_FACE_ID;
import static com.google.android.gms.vision.CameraSource.CAMERA_FACING_FRONT;
import static org.bytedeco.javacpp.opencv_core.CV_32SC1;
import static org.bytedeco.javacpp.opencv_imgcodecs.CV_LOAD_IMAGE_GRAYSCALE;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;


public class FaceRecognitionActivity extends AppCompatActivity {
    private static final String TAG = FaceRecognitionActivity.class.getSimpleName();
    SurfaceView surfaceView;
    FaceOverlay faceOverlay;
    CustomFaceDetector faceDetector;
    CameraSource cs;
    ImageView testImg;
    ImageView testImg1;
    ImageView testImg2;
    ImageView testImg3;
    TextView resultTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_recognition);
        clearFiles();
        showFiles();

        testImg = findViewById(R.id.test_face_img);
        testImg1 = findViewById(R.id.test_face_img1);
        testImg2 = findViewById(R.id.test_face_img2);
        testImg3 = findViewById(R.id.test_face_img3);
        resultTxt = findViewById(R.id.face_recog_result_txt);

        surfaceView = findViewById(R.id.face_surface_view);
        faceOverlay = findViewById(R.id.face_overlay);
        saveDrawableFacesToInternalStorage();

        faceDetector = new CustomFaceDetector(new FaceDetector.Builder(this)
                .setTrackingEnabled(true)
                .setProminentFaceOnly(true)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .build(), this);


        //determines when the detector receives a result
        faceDetector.setProcessor(new Detector.Processor<Face>() {
            boolean performRecog = true;

            @Override
            public void release() {
                Log.v(TAG, "in release");
            }

            /**
             * This method does not run on the UI thread
             * @param detections
             */
            @Override
            public void receiveDetections(Detector.Detections<Face> detections) {

                final SparseArray<Face> faces = detections.getDetectedItems();
                if (faces.size() > 0) {
                    Face face = faces.valueAt(0);
                    //Log.v(TAG, "detector is setting face " + face.getHeight() + " " + face.getWidth());
                    faceOverlay.setFace(face);

                    if(faceDetector.getImgNum() == NUM_CAPTUREDS && performRecog) {
                        Log.v(TAG, "face detector img num is 4, stopping face detection");
                        new PerformRecognitionTask().execute();
                        performRecog= false;
                    }
                    else if(performRecog){
                        resultTxt.post(new Runnable() {
                            @Override
                            public void run() {
                                String txt = "Running face detection " + faceDetector.getImgNum();
                                resultTxt.setText(txt);
                            }
                        });
                    }


                }

            }
        });


        cs = new CameraSource.Builder(this, faceDetector)
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

    private void clearFiles() {
        Log.v(TAG, "-------------deleting all files in training dir");
        File dir = new File(this.getFilesDir(), Utils.TRAIN_DIR);
        File[] files = dir.listFiles();
        for(File file : files) {
            file.delete();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cs.release();
        faceDetector.release();
    }

    /**
     * FOR DEBUG ONLY
     * Seems like we can only get 4 good pics, the 5th goes to hell
     * @param
     */
    public void showPics(View view) {
        cs.release();

        Bitmap b = Utils.retrieveImg(true, YOUR_FACE_ID, "0", getApplicationContext());
        if(b != null && testImg != null)
            testImg.setImageBitmap(b);
        Bitmap b1 = Utils.retrieveImg(true, YOUR_FACE_ID, "1", getApplicationContext());
        if(b1 != null && testImg1 != null)
            testImg1.setImageBitmap(b1);
        Bitmap b2 = Utils.retrieveImg(true, YOUR_FACE_ID,"2", getApplicationContext());
        Bitmap b3 = Utils.retrieveImg(true, YOUR_FACE_ID,"3", getApplicationContext());
        testImg2.setImageBitmap(b2);
        testImg3.setImageBitmap(b3);
    }



    private int recognizeFacesOld() {
        Log.v(TAG, "Performing Face Recognition...");
        String trainingDir = this.getFilesDir().getAbsolutePath() + "/" + Utils.TRAIN_DIR;

        String testingFilePath = this.getFilesDir().getAbsolutePath()
                + "/" + Utils.TEST_DIR
                + "/" + Utils.PASSPORT_FACE_ID
                + "-face_"
                + Utils.PASSPORT_SAMPLE_NUM
                + ".png";

        opencv_core.Mat testImg = imread(testingFilePath, CV_LOAD_IMAGE_GRAYSCALE);

        File trainRoot = new File(trainingDir);

        FilenameFilter pngFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".png");
            }
        };

        File[] imageFiles = trainRoot.listFiles(pngFilter);

        int numImages = imageFiles.length; //todo keep an eye on this!!
        opencv_core.MatVector images = new opencv_core.MatVector(numImages);

        opencv_core.Mat labels = new opencv_core.Mat(numImages, 1, CV_32SC1);
        IntBuffer labelsBuf = labels.createBuffer();

        int counter = 0;
        Log.v(TAG, "loading files...");
        for (File image : imageFiles) {

            opencv_core.Mat img = imread(image.getAbsolutePath(), CV_LOAD_IMAGE_GRAYSCALE);
            int faceID = Integer.parseInt(image.getName().split("\\-")[0]);
            images.put(counter, img);
            labelsBuf.put(counter, faceID);
            Log.v(TAG, "training image " + image.getName() + " is read. Person ID " + faceID);
            counter++;

        }

        opencv_face.FaceRecognizer faceRecognizer = opencv_face.LBPHFaceRecognizer.create();
        faceRecognizer.train(images, labels);
        Log.v(TAG, "training done");


        Log.v(TAG, "predicting test img");
        IntPointer predictedFaceID = new IntPointer(1);
        DoublePointer confidence = new DoublePointer(1);
        faceRecognizer.predict(testImg, predictedFaceID, confidence);

        int predictedLabel = predictedFaceID.get(0);
        Log.v(TAG, "predicted label is " + predictedLabel);
        Log.v(TAG, "confidence is: " + confidence.get());

        return predictedLabel;
    }

    private int recognizeFaces() {
        return 0;
    }

    private void showFiles() {
        Log.v(TAG, "FILES in filesdir -------------------");
        File[] files = this.getFilesDir().listFiles();
        Log.d("Files", "Size: "+ files.length);
        for (int i = 0; i < files.length; i++)
        {
            Log.d("Files", "FileName:" + files[i].getName());
        }
    }

    private void saveDrawableFacesToInternalStorage() {
        AssetManager assetManager = this.getAssets();
        try {
            String[] files = assetManager.list(Utils.INTERNET_FACES_PATH);

            for (String file : files) {
                Log.v(TAG, "internet files opening:  " + file);
                String pathname = Utils.INTERNET_FACES_PATH + "/" + file;
                BitmapDrawable d = (BitmapDrawable) Drawable.createFromStream(assetManager.open(pathname), null);

                String faceID = file.split("\\-")[0];
                Log.v(TAG, "saving internet face with id " + faceID);
                String namewopng = file.split("\\.")[0];
                String sampleNumber = Character.toString(namewopng.charAt(namewopng.length() - 1));
                Utils.saveImg(
                        true,
                        faceID,
                        sampleNumber,
                        d.getBitmap(),
                        this);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //todo make async tasks static to avoid leaks, add weak reference to activity
    private class PerformRecognitionTask extends AsyncTask<Void, Integer, Integer> {

        @Override
        protected Integer doInBackground(Void... params) {
            return recognizeFaces();
        }

        @Override
        protected void onPostExecute(final Integer predictedLabel) {

            new CountDownTimer(1000, 1000) {
                String txt = "You are matched with label: " + predictedLabel;
                public void onTick(long millisUntilFinished) {
                    String tickInfo = txt + " count down " + millisUntilFinished/1000;
                    resultTxt.setText(tickInfo);
                }

                public void onFinish() {
                    if(predictedLabel == Integer.parseInt(Utils.YOUR_FACE_ID)) {
                        setResult(Activity.RESULT_OK, new Intent());
                    }
                    else {
                        setResult(Activity.RESULT_CANCELED, new Intent());
                    }

                    finish(); //this should release detector
                }
            }.start();
        }
    }
}
