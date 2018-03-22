package com.example.lucyzhao.votingapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

/**
 * Created by LucyZhao on 2018/3/8.
 */

public class Utils {
    private static final String TAG = Utils.class.getSimpleName();
    public static final String QR_RESULT = "qr_result";
    public static final String NFC_RESULT = "nfc_result";

    public static final String COMM_NFC_ID = "NFCID";
    public static final String COMM_QR_CODE = "QRCODE";
    public static final String COMM_CANDIDATE_ID = "CANDIDATEID";

    public static final String AVATAR_URL = "avatar_url";
    public static final String NAME = "name";
    public static final String BIO = "bio";

    public static final String PASSPORT_BIRTHDATE_STR = "Birthdate";
    public static final String PASSPORT_NATIONALITY_STR = "Nationality";
    public static final String ALLOWABLE_NATIONALITY = "CHN";

    public static final int ALLOWABLE_AGE = 18;

    public static final int NUM_CAPTUREDS = 4;

    public static final String INTERNET_FACES_PATH = "internet_faces";
    public static final String YOUR_FACE_ID = "1";
    public static final String PASSPORT_FACE_ID = "0";
    public static final String PASSPORT_SAMPLE_NUM = "0";

    public static final String TRAIN_DIR = "train_dir";
    public static final String TEST_DIR = "test_dir";

    public static boolean checkNationalityEligiblity(String nationality) {
        return nationality.equals(ALLOWABLE_NATIONALITY);
    }

    /**
     * Birthday in the format yymmdd
     *
     * @param birthDate
     * @return
     */
    public static boolean checkAgeEligibility(String birthDate) {

        int year = Integer.parseInt(birthDate.substring(0, 2));
        int month = Integer.parseInt(birthDate.substring(2, 4));
        int date = Integer.parseInt(birthDate.substring(4, 6));

        Calendar bDay = Calendar.getInstance();
        Calendar today = Calendar.getInstance();

        bDay.set(year, month, date);
        int age = today.get(Calendar.YEAR) - bDay.get(Calendar.YEAR);
        if (today.get(Calendar.DAY_OF_YEAR) < bDay.get(Calendar.DAY_OF_YEAR)) age--;

        return age >= ALLOWABLE_AGE;
    }

    /**
     * @param sampleNumber in the form of 1,2,3
     * @param bitmap
     * @param context
     * @throws IOException
     */
    public static void saveImg(boolean training, String faceID, String sampleNumber, Bitmap bitmap, Context context) throws IOException {
        bitmap = cropFace(bitmap, context);

        String filename = faceID + "-face_" + sampleNumber + ".png";
        Log.v(TAG, "saving image..." + filename + " for training? " + training);

        File dir;
        if (training) {
            dir = new File(context.getFilesDir(), TRAIN_DIR);
        } else {
            dir = new File(context.getFilesDir(), TEST_DIR);
        }

        boolean ret = true;
        if (!dir.exists())
            ret = dir.mkdirs();
        if (ret) {
            FileOutputStream fileOutputStream = new FileOutputStream(new File(dir, filename));
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, fileOutputStream);
            Log.v(TAG, "file saved");
        } else {
            Log.v(TAG, "file saving failed");
        }

    }

    public static Bitmap retrieveImg(boolean training, String faceID, String num, Context context) {
        String folder;
        if (training) {
            folder = TRAIN_DIR;
        } else {
            folder = TEST_DIR;
        }
        String filename = "/" + folder + "/" + faceID + "-face_" + num + ".png";
        Log.v(TAG, "image " + filename + " retrieved!");
        Bitmap b = BitmapFactory.decodeFile(context.getFilesDir().getAbsolutePath() + filename);
        if (b == null) {
            Log.v(TAG, "retrieved b is null!");
        }
        return b;
    }

    public static Bitmap cropFace(Bitmap origB, Context context) {
        FaceDetector faceDetector = new
                FaceDetector.Builder(context).setTrackingEnabled(false)
                .build();
        if (!faceDetector.isOperational()) {
            Log.v(TAG, "face detector is not operational, sad");
        }
        Frame frame = new Frame.Builder().setBitmap(origB).build();
        SparseArray<Face> faces = faceDetector.detect(frame);
        if(faces.size() > 0) {
            Face face = faces.valueAt(0);
            int x1 = (int) face.getPosition().x;
            int y1 = (int) face.getPosition().y;
            float x2 = x1 + face.getWidth();
            float y2 = y1 + face.getHeight();

            if(x1 > 0
                    && y1 > 0
                    && x2 < frame.getMetadata().getWidth()
                    && y2 < frame.getMetadata().getHeight()
                    ) {
                Log.v(TAG, "face cropped");
                origB = Bitmap.createBitmap(origB, x1, y1, (int) face.getWidth(), (int) face.getHeight());
            }
            else {
                Log.v(TAG, "face out of bound, orig img returned");
            }


        }
        return origB;
    }


}
