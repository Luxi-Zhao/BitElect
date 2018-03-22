package com.example.lucyzhao.votingapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

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

    public static final int NUM_IMG_FILES = 4;

    public static final String INTERNET_FACES_PATH = "internet_faces";
    public static final String YOUR_FACE_ID = "1";

    public static boolean checkNationalityEligiblity(String nationality) {
        return nationality.equals(ALLOWABLE_NATIONALITY);
    }

    /**
     * Birthday in the format yymmdd
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
        if(today.get(Calendar.DAY_OF_YEAR) < bDay.get(Calendar.DAY_OF_YEAR)) age--;

        return age >= ALLOWABLE_AGE;
    }

    /**
     *
     * @param sampleNumber in the form of 1,2,3
     * @param bitmap
     * @param context
     * @throws IOException
     */
    public static void saveImg(String faceID, String sampleNumber, Bitmap bitmap, Context context) throws IOException {

        String filename = faceID + "-face_" + sampleNumber + ".png";
        Log.v(TAG, "saving image..." + filename);
        java.io.FileOutputStream out = context.openFileOutput(filename, Context.MODE_PRIVATE);
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
    }

    public static Bitmap retrieveImg(String faceID, String num, Context context) {
        //return BitmapFactory.decodeFile(path);
        String filename = "/" + faceID + "-face_" + num + ".png";
        Log.v(TAG, "image " + filename + " retrieved!");
        Bitmap b = BitmapFactory.decodeFile(context.getFilesDir().getAbsolutePath() + filename);
        if(b == null) {
            Log.v(TAG, "retrieved b is null!");
        }
        return b;
    }


}
