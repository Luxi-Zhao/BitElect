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
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

import static com.example.lucyzhao.votingapp.Utils.NUM_CAPTURES;
import static com.example.lucyzhao.votingapp.Utils.PASSPORT_FACE_ID;
import static com.example.lucyzhao.votingapp.Utils.PASSPORT_SAMPLE_NUM;
import static com.example.lucyzhao.votingapp.Utils.YOUR_FACE_ID;
import static com.google.android.gms.vision.CameraSource.CAMERA_FACING_FRONT;


public class FaceRecognitionActivity extends AppCompatActivity {
    private static final String TAG = FaceRecognitionActivity.class.getSimpleName();
    private static final int COUNT_DOWN = 5000;
    private static final float SIM_RATIO_THRESHOLD = (float) 0.4;
    SurfaceView surfaceView;
    FaceOverlay faceOverlay;
    CustomFaceDetector faceDetector;
    CameraSource cs;
    TextView resultTxt;
    PerformRecognitionTask performRecognitionTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_recognition);
        Utils.clearFiles(this);
        showFiles();

        resultTxt = findViewById(R.id.face_recog_result_txt);
        surfaceView = findViewById(R.id.face_surface_view);
        faceOverlay = findViewById(R.id.face_overlay);

        performRecognitionTask = new PerformRecognitionTask(this);

        faceDetector = new CustomFaceDetector(new FaceDetector.Builder(this)
                .setTrackingEnabled(true)
                .setProminentFaceOnly(true)
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
                    final Face face = faces.valueAt(0);
                    faceOverlay.setFace(face);

                    if (faceDetector.getImgNum() == NUM_CAPTURES && performRecog) {
                        Log.v(TAG, "stopping face detection");
                        performRecognitionTask.execute();
                        performRecog = false;
                    } else if (performRecog) {
                        resultTxt.post(new Runnable() {
                            @Override
                            public void run() {
                                String txt = "Running face detection " + (faceDetector.getImgNum() + 1);
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


    @Override
    protected void onDestroy() {
        super.onDestroy();
        cs.release();
        faceDetector.release();
    }

    private static float recognizeFaces(Context context) {
        Bitmap passportPhoto = Utils.retrieveImg(false,
                PASSPORT_FACE_ID,
                PASSPORT_SAMPLE_NUM,
                context);

        float maxRatio = 0;
        for (int i = 0; i < NUM_CAPTURES; i++) {
            Bitmap cameraCapture = Utils.retrieveImg(true,
                    YOUR_FACE_ID,
                    Integer.toString(i),
                    context);
            float ratio = FaceComparer.compareImgs(passportPhoto, cameraCapture);
            if (ratio > maxRatio) {
                maxRatio = ratio;
            }
        }

        return maxRatio;
    }


    /**
     * FOR DEBUG ONLY
     */
    private void showFiles() {
        Log.v(TAG, "FILES in filesdir -------------------");
        File[] files = this.getFilesDir().listFiles();
        Log.d("Files", "Size: " + files.length);
        for (int i = 0; i < files.length; i++) {
            Log.d("Files", "FileName:" + files[i].getName());
        }
    }

    /**
     * Called in onCreate to save assets to internal storage
     */
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

    private static class PerformRecognitionTask extends AsyncTask<Void, Integer, Float> {
        private WeakReference<FaceRecognitionActivity> activityRef;

        PerformRecognitionTask(FaceRecognitionActivity context) {
            activityRef = new WeakReference<>(context);
        }

        @Override
        protected Float doInBackground(Void... params) {
            FaceRecognitionActivity activity = activityRef.get();
            if (activity == null || activity.isFinishing()) return (float) 0.0;
            return recognizeFaces(activityRef.get().getApplicationContext());
        }

        @Override
        protected void onPostExecute(final Float simRatio) {

            new CountDownTimer(COUNT_DOWN, 1000) {
                String txt = "Similarity ratio: " + simRatio;

                public void onTick(long millisUntilFinished) {
                    FaceRecognitionActivity activity = activityRef.get();
                    if (activity == null || activity.isFinishing()) return;
                    String tickInfo = txt + "\n Going back in " + millisUntilFinished / 1000 + " seconds";
                    activity.resultTxt.setText(tickInfo);

                }

                public void onFinish() {
                    FaceRecognitionActivity activity = activityRef.get();
                    if (activity == null || activity.isFinishing()) return;
                    if (simRatio > SIM_RATIO_THRESHOLD) {
                        activity.setResult(Activity.RESULT_OK, new Intent());
                    } else {
                        activity.setResult(Activity.RESULT_CANCELED, new Intent());
                    }

                    activity.finish(); //this should release detector
                }
            }.start();
        }
    }
}
