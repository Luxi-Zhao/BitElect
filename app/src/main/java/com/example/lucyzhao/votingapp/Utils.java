package com.example.lucyzhao.votingapp;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.util.Log;
import android.util.SparseArray;
import android.widget.TextView;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Random;

/**
 * Created by LucyZhao on 2018/3/8.
 */

public class Utils {
    private static final String TAG = Utils.class.getSimpleName();
    public static final String QR_RESULT = "qr_result";
    public static final String NFC_RESULT = "nfc_result";

    /*----------- DE1-SOC COMM --------------*/
    public static final String VOTING_URL = "http://192.168.43.27/?";
    public static final String COMM_RESPONSE_TYPE = "RESPONSETYPE";
    public static final String COMM_NFC_ID = "NFCID";
    public static final String COMM_QR_CODE = "QRCODE";
    public static final String COMM_CANDIDATE_ID = "CANDIDATEID";



    public static final String COMM_BLOCKID = "BLOCKID";
    public static final String COMM_REQ_CHAIN = "CHAINREQUEST";
    public static final String COMM_BLOCK_RESPONSE = "BLOCKRESPONSE";
    public static final String COMM_BLOCK_VALID = "BLOCKVALID";
    public static final String COMM_BLOCK_PREV_HASH= "PREVHASH";
    public static final String COMM_BLOCK_HASH = "HASH";
    public static final String COMM_BLOCK_CUMU_CRYPT = "CUMULATIVECRYPT";
    public static final String COMM_BLOCK_TOTAL_NUM = "TOTALBLOCKS";

    public static final String COMM_REQ_RESULT = "RESULTSREQUEST";
    public static final String COMM_RESULT_RESPONSE = "RESULTSRESPONSE";
    public static final String COMM_RESULT_VALID = "RESULTSVALID";
    public static final String COMM_CAND1_FN = "CAND1FN";
    public static final String COMM_CAND1_LN = "CAND1LN";
    public static final String COMM_CAND2_FN = "CAND2FN";
    public static final String COMM_CAND2_LN = "CAND2LN";
    public static final String COMM_RESULT_CAND1V = "CAND1VOTES";
    public static final String COMM_RESULT_CAND2V = "CAND2VOTES";

    public static final String COMM_REQ_KEY = "KEYREQUEST";
    public static final String COMM_KEY_MSG = "REJECTIONMESSAGE";
    public static final String COMM_KEY_VALID = "KEYACCEPTED";
    public static final String COMM_KEY_KEY = "PRIVVAL";
    public static final String COMM_KEY_TYPE = "PRIVIDENT";

    public static final String TRUE = "True";
    public static final String FALSE = "False";

    /*----------- GitHub --------------*/
    public static final String AVATAR_URL = "avatar_url";
    public static final String NAME = "name";
    public static final String BIO = "bio";

    public static final String PASSPORT_BIRTHDATE_STR = "Birthdate";
    public static final String PASSPORT_NATIONALITY_STR = "Nationality";
    public static final String ALLOWABLE_NATIONALITY = "CHN";
    public static final int ALLOWABLE_AGE = 18;

    public static final int NUM_CAPTURES = 4;

    public static final String INTERNET_FACES_PATH = "internet_faces";
    public static final String YOUR_FACE_ID = "1";
    public static final String PASSPORT_FACE_ID = "0";
    public static final String PASSPORT_SAMPLE_NUM = "0";

    private static final String TRAIN_DIR = "train_dir";
    private static final String TEST_DIR = "test_dir";


    ///////////////////////////////////////////////////////////////////////
    ////////////////PASSPORT/ELIGIBILITY CHECKING//////////////////////////
    ///////////////////////////////////////////////////////////////////////

    public static boolean checkNationalityEligiblity(String nationality) {
        return true;
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

    public static void savePassportInfoToPref(Context context, String docNumStr, String birthDateStr, String expiryDateStr) {
        SharedPreferences sharedPref = context
                .getSharedPreferences(context.getString(R.string.pref_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(context.getString(R.string.shared_pref_doc_num), docNumStr);
        editor.putString(context.getString(R.string.shared_pref_birthdate), birthDateStr);
        editor.putString(context.getString(R.string.shared_pref_expirydate), expiryDateStr);
        editor.apply(); //asynchronously save to pref
        Log.v(TAG, "saved to pref");
    }

    public static String getDocNum(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.pref_file_key), Context.MODE_PRIVATE);
        return sharedPref.getString(context.getString(R.string.shared_pref_doc_num), "");
    }

    ///////////////////////////////////////////////////////////////////////
    ///////////////////////////IMG/FILE SAVING/////////////////////////////
    ///////////////////////////////////////////////////////////////////////

    public static void clearFiles(Context context) {
        Log.v(TAG, "-------------deleting all files in training dir");
        File dir = new File(context.getFilesDir(), Utils.TRAIN_DIR);
        File[] files = dir.listFiles();
        if(files != null) {
            for (File file : files) {
                file.delete();
            }
        }
    }

    /**
     * FOR DEBUG ONLY
     */
    public static void showFiles(Context context) {
        Log.v(TAG, "FILES in filesdir -------------------");
        File[] files = context.getFilesDir().listFiles();
        Log.d("Files", "Size: " + files.length);
        for (int i = 0; i < files.length; i++) {
            Log.d("Files", "FileName:" + files[i].getName());
        }
    }

    /**
     * @param sampleNumber in the form of 1,2,3
     * @param bitmap
     * @param context
     * @throws IOException
     */
    public static void saveImg(boolean training, String faceID, String sampleNumber, Bitmap bitmap, Context context) throws IOException {
        bitmap = cropFace(bitmap, context);
        if (bitmap == null) return;
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
        filename = context.getFilesDir().getAbsolutePath() + filename;
        File file = new File(filename);
        if(!file.exists()) {
            Log.v(TAG, "FILE DOES NOT EXIST");
            //return null;
        }

        Log.v(TAG, "image " + filename + " retrieved!");

        Bitmap b = BitmapFactory.decodeFile(file.getAbsolutePath());
        if (b == null) {
            Log.v(TAG, "retrieved b is null!");
        }
        return b;
    }

    ///////////////////////////////////////////////////////////////////////
    ///////////////////////////IMG MANIPULATION////////////////////////////
    ///////////////////////////////////////////////////////////////////////

    public static Bitmap cropFace(Bitmap origB, Context context) {
        FaceDetector faceDetector = new
                FaceDetector.Builder(context).setTrackingEnabled(false)
                .build();
        if (!faceDetector.isOperational()) {
            Log.v(TAG, "face detector is not operational, sad");
            return null;
        }
        Frame frame = new Frame.Builder().setBitmap(origB).build();
        SparseArray<Face> faces = faceDetector.detect(frame);
        if (faces.size() > 0) {
            Face face = faces.valueAt(0);
            int x1 = (int) face.getPosition().x;
            int y1 = (int) face.getPosition().y;
            float x2 = x1 + face.getWidth();
            float y2 = y1 + face.getHeight();

            if (x1 > 0
                    && y1 > 0
                    && x2 < frame.getMetadata().getWidth()
                    && y2 < frame.getMetadata().getHeight()
                    ) {
                Log.v(TAG, "face cropped");
                origB = Bitmap.createBitmap(origB, x1, y1, (int) face.getWidth(), (int) face.getHeight());
            } else {
                Log.v(TAG, "face out of bound, orig img returned");
            }


        }
        return origB;
    }

    ///////////////////////////////////////////////////////////////////////
    ///////////////////////////////ENCRYPTION//////////////////////////////
    ///////////////////////////////////////////////////////////////////////
    public static String encryptVote(int candidateID) {
        int zeroindex = candidateID - 1;

        Random rand = new Random();

        BigInteger publicKey = BigInteger.valueOf(13969);
        BigInteger publicKeysq = publicKey.pow(2);
        BigInteger plainText = BigInteger.valueOf(zeroindex);

        BigInteger num;

        while (true) {
            num = new BigInteger((int) Math.round(Math.log(publicKey.doubleValue()) / Math.log(2)), 10, rand);
            if (num.compareTo(BigInteger.ZERO) == 1 && num.compareTo(publicKey) == -1) {
                break;
            }
        }

        BigInteger temp = num.modPow(publicKey, publicKeysq);
        BigInteger cipher = publicKey.add(BigInteger.valueOf(1)).modPow(plainText, publicKeysq).multiply(temp).mod(publicKeysq);

        return Integer.toString(cipher.intValue());
    }


    ///////////////////////////////////////////////////////////////////////
    ///////////////////////////////////UI//////////////////////////////////
    ///////////////////////////////////////////////////////////////////////

    /**
     * Only usable for activities with layouts directly associated with them
     * Does not apply to fragments, navigation drawers, etc.
     * @param activity
     * @param ids TextView ids
     */
    public static void setTypeFace(Activity activity, int... ids) {
        Typeface typeface = activity.getResources().getFont(R.font.quicksand);
        for(int id : ids) {
            TextView textView = activity.findViewById(id);
            textView.setTypeface(typeface);
        }
    }

}
