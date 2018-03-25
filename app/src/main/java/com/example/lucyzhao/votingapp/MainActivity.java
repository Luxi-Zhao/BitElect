package com.example.lucyzhao.votingapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.FaceDetector;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.example.lucyzhao.votingapp.Utils.COMM_CANDIDATE_ID;
import static com.example.lucyzhao.votingapp.Utils.COMM_NFC_ID;
import static com.example.lucyzhao.votingapp.Utils.COMM_QR_CODE;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int QR_ACTIVITY_REQ_CODE = 111;
    private static final int FACE_ACTIVITY_REQ_CODE = 250;
    private static final String VOTING_URL = "http://192.168.43.27/?";

    // UI element
    AlertDialog.Builder votingAlertBuilder;

    // UI task tracking that tracks how many tasks the user has completed
    private Vote myVote = Vote.getInstance();
    private static String candidateName;
    private static int tasksCompleted = 0;
    private UITaskManager taskManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.v(TAG, "in onCreate");

        // UI elements
        votingAlertBuilder = new AlertDialog.Builder(this);
        votingAlertBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                submitVote();
            }
        });

        taskManager = new UITaskManager();
        // handle UI appropriately when only some data is collected
        Log.v(TAG, "tasks completed is: " + tasksCompleted);
        taskManager.onTaskCompleted(tasksCompleted);
        // ignore any nfc feedback if passport is already scanned
        if (tasksCompleted > 2) {
            return;
        }


        // check NFC scanning result
        String nfc_result = getIntent().getStringExtra(Utils.NFC_RESULT);
        if (nfc_result != null) {
            Log.v(TAG, "result of nfc scanning is:" + nfc_result);
            //myVote.setNfcID(nfc_result);
            setPassportScanTaskCompleted(nfc_result);
        } else {
            Log.v(TAG, "nfc scanning not performed");
            taskManager.startTasks();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        taskManager.onTaskCompleted(tasksCompleted);
    }

    //////////////////////////////////////////////////////////////
    //////////////////UI TASK FLOW MANAGEMENT/////////////////////
    //////////////////////////////////////////////////////////////

    private void setEnterPassportInfoTaskCompleted(String docNumStr, String birthDateStr, String expiryDateStr) {
        SharedPreferences sharedPref = this
                .getSharedPreferences(getString(R.string.pref_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.shared_pref_doc_num), docNumStr);
        editor.putString(getString(R.string.shared_pref_birthdate), birthDateStr);
        editor.putString(getString(R.string.shared_pref_expirydate), expiryDateStr);
        editor.apply(); //asynchronously save to pref
        Log.v(TAG, "saved to pref");

        taskManager.onTaskCompleted(1);
    }


    private void setPassportScanTaskCompleted(String nfc_result) {
        myVote.setNfcID(nfc_result);
        taskManager.onTaskCompleted(2);
    }

    private void setFaceTaskCompleted(boolean match) {
        taskManager.onTaskCompleted(3);
    }

    private void setQRTaskCompleted(String qr_result) {
        myVote.setQrCode(qr_result);
        taskManager.onTaskCompleted(4);
    }


    protected void setCandSelTaskCompleted(String candID, String candName) {
        myVote.setCandidateID(candID);
        candidateName = candName;
        taskManager.onTaskCompleted(5);
    }

    private void setVotingTaskCompleted() {
        taskManager.onTaskCompleted(6);
    }

    /**
     * Updates the UI according to how many tasks the user has completed
     * <p>
     * Get children views from linear layout
     * Initially, all elements are disabled except for the first one
     * When each task is completed, enable the next element and disable all previous elements
     */
    private class UITaskManager {
        private List<PulsingButton> allBtns;
        private List<View> allLines;

        // must be called in onCreate()
        UITaskManager() {
            allBtns = new ArrayList<>();
            allLines = new ArrayList<>();
            LinearLayout ll = findViewById(R.id.task_layout);
            //not sure tho
            for (int i = 0; i < ll.getChildCount(); i++) {
                View view = ll.getChildAt(i);
                if (i % 2 == 0)
                    allBtns.add((PulsingButton) view);
                else {
                    allLines.add(view);
                }
            }
        }

        void startTasks() {
            allBtns.get(0).enablePulsingButton();
        }

        void onTaskCompleted(int taskNum) {
            if (taskNum == 0) return;
            for (int i = 0; i < taskNum; i++) {
                allBtns.get(i).setCompleted();
            }

            for (int i = 0; i < taskNum - 1; i++) {
                allLines.get(i)
                        .setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorBtnCompleted));
            }

            // ---------- for debug only
//            for (int i = taskNum + 1; i < allBtns.size(); i++) {
//                allBtns.get(i).setUncompleted();
//            }
//
//            for (int i = taskNum - 1; i < allLines.size(); i++) {
//                allLines.get(i)
//                        .setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
//            }
            // ----------

            if (taskNum < allBtns.size()) {
                allBtns.get(taskNum).enablePulsingButton();
            }

            tasksCompleted = taskNum;
        }
    }


    //////////////////////////////////////////////////////////////
    ////////////////////////////QR CODE///////////////////////////
    //////////////////////////////////////////////////////////////

    public void scanQRCode(View view) {
        //Intent intent = new Intent(this, QRCodeActivity.class); todo change this backckckckk
        Intent intent = new Intent(this, TestActivity.class);
        startActivityForResult(intent, QR_ACTIVITY_REQ_CODE);
    }



    //////////////////////////////////////////////////////////////
    ////////////////////////////VOTING////////////////////////////
    //////////////////////////////////////////////////////////////

    public void selectCandidate(View view) {
        new CandidateSelFragment().show(getFragmentManager(), "selectCandidate");
    }

    public void vote(View view) {
        votingAlertBuilder.setMessage("Are you sure you want to choose " + candidateName + "?");
        AlertDialog dialog = votingAlertBuilder.create();
        dialog.show();
    }

    private void submitVote() {
        if (!myVote.checkAllFieldsExist()) {
            Toast.makeText(getApplicationContext(), "please complete all steps", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestQueue queue = Volley.newRequestQueue(this);

        String encryptedCandidateID = encryptVote(Integer.valueOf(myVote.getCandidateID()));

        String url = VOTING_URL + COMM_QR_CODE + "=" + myVote.getQrCode() + "&"
                + COMM_NFC_ID + "=" + myVote.getNfcID() + "&"
                + COMM_CANDIDATE_ID + "=" + encryptedCandidateID;
        Log.v(TAG, "url is: " + url);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //todo
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //todo
            }
        });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);

        // UI
        setVotingTaskCompleted();
    }

    private String encryptVote(int candidateID) {
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

    private static class Vote {
        private static Vote vote;

        static Vote getInstance() {
            if (vote == null) {
                vote = new Vote();
            }
            return vote;
        }

        String nfcID = "", qrCode = "", candidateID = "";

        void setNfcID(String nfcID) {
            this.nfcID = nfcID;
        }

        void setQrCode(String qrCode) {
            this.qrCode = qrCode;
        }

        void setCandidateID(String candidateID) {
            this.candidateID = candidateID;
        }

        String getNfcID() {
            return nfcID;
        }

        String getQrCode() {
            return qrCode;
        }

        String getCandidateID() {
            return candidateID;
        }

        boolean checkAllFieldsExist() {
            return !nfcID.equals("") && !qrCode.equals("") && !candidateID.equals("");
        }
    }


    //////////////////////////////////////////////////////////////
    ////////////////////////////PASSPORT//////////////////////////
    //////////////////////////////////////////////////////////////

    public void enterPassportInfo(View view) {
        new PassportInfoPromptFragment().show(getFragmentManager(), "enterPassportInfo");
    }

    public void scanPassportPrompt(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setMessage(R.string.scan_passport_prompt);
        builder.create().show();
    }

    public static class PassportInfoPromptFragment extends DialogFragment implements View.OnClickListener {
        private EditText birthDate;
        private EditText expiryDate;
        private EditText docNum;
        private Button okBtn;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View fragment = inflater.inflate(R.layout.fragment_passport_info, container, false);
            birthDate = fragment.findViewById(R.id.birthdate_edit_text);
            expiryDate = fragment.findViewById(R.id.expirydate_edit_text);
            docNum = fragment.findViewById(R.id.passport_num_edit_text);
            okBtn = fragment.findViewById(R.id.save_info_ok_btn);
            okBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    String birthDateStr = birthDate.getText().toString();
                    String expiryDateStr = expiryDate.getText().toString();
                    String docNumStr = docNum.getText().toString().toUpperCase();
                    if (birthDateStr.length() != 6 || expiryDateStr.length() != 6 || docNumStr.isEmpty()) {
                        Toast.makeText(getContext(), "Incorrect Info", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    ((MainActivity) getActivity())
                            .setEnterPassportInfoTaskCompleted(docNumStr, birthDateStr, expiryDateStr);
                    dismiss();
                }
            });

            return fragment;
        }

        @Override
        public void onClick(View view) {
            dismiss();
        }
    }

    //////////////////////////////////////////////////////////////
    ///////////////////////FACE RECOGNITION///////////////////////
    //////////////////////////////////////////////////////////////

    public void scanFacePrompt(View view) {
        Intent intent = new Intent(this, FaceRecognitionActivity.class);
        startActivityForResult(intent, FACE_ACTIVITY_REQ_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case (QR_ACTIVITY_REQ_CODE): {
                if (resultCode == Activity.RESULT_OK) {
                    // check QR Code scanning result
                    String qr_result = data.getStringExtra(Utils.QR_RESULT);
                    if (qr_result != null) {
                        Log.v(TAG, "result of qr scanning is:" + qr_result);
                        setQRTaskCompleted(qr_result);
                    } else {
                        Log.v(TAG, "qr scanning returned null!");
                    }
                }
                break;
            }

            case (FACE_ACTIVITY_REQ_CODE): {
                if (resultCode == Activity.RESULT_OK) {

                    setFaceTaskCompleted(true);
                }
                else {
                    setFaceTaskCompleted(false);
                }
                break;
            }

        }
    }


}