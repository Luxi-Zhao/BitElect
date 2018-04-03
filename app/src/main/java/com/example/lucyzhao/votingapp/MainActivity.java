package com.example.lucyzhao.votingapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonObject;
import com.koushikdutta.ion.Ion;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static com.example.lucyzhao.votingapp.Utils.COMM_CANDIDATE_ID;
import static com.example.lucyzhao.votingapp.Utils.COMM_NFC_ID;
import static com.example.lucyzhao.votingapp.Utils.COMM_QR_CODE;
import static com.example.lucyzhao.votingapp.Utils.VOTING_URL;
import static java.lang.Thread.sleep;

public class MainActivity extends NavActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int QR_ACTIVITY_REQ_CODE = 111;
    private static final int FACE_ACTIVITY_REQ_CODE = 250;


    // UI element
    AlertDialog.Builder votingAlertBuilder;

    // vote related
    private Vote myVote = Vote.getInstance();
    private static String candidateName;

    // UI task tracking that tracks how many tasks the user has completed
    private static int tasksCompleted = 0;
    private static boolean voteCompleted = false;
    private UITaskManager taskManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.onCreateDrawer(R.layout.activity_main);
        Utils.setTypeFace(this, R.id.main_title_txt, R.id.main_title_txt2);
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
        taskManager.initUI();

        // view images used for comparison on long click
        Button faceRecogBtn = findViewById(R.id.task3btn);
        faceRecogBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                startActivity(new Intent(getApplicationContext(), TestActivity.class));
                return false;
            }
        });

        // ignore any nfc feedback if passport is already scanned
        if (tasksCompleted > 1) {
            return;
        }
        // check NFC scanning result
        String nfc_result = getIntent().getStringExtra(Utils.NFC_RESULT);
        if (nfc_result != null) {
            Log.v(TAG, "result of nfc scanning is:" + nfc_result);
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

    public static void setTasksCompleted(int num) {
        tasksCompleted = num;
    }

    public static void setVoteCompleted(int bool) {
        voteCompleted = false;
    }

    private void setPassportScanTaskCompleted(String nfc_result) {
        myVote.setNfcID(nfc_result);
        taskManager.onTaskCompleted(1);
    }

    private void setFaceTaskCompleted(boolean match) {
        if(match) {
            taskManager.onTaskCompleted(2);
        }
    }

    private void setQRTaskCompleted(String qr_result) {
        myVote.setQrCode(qr_result);
        taskManager.onTaskCompleted(3);
        voteCompleted = false;
        showSelectCandFrag();
    }


    protected void setCandSelTaskCompleted(String candID, String candName) {
        myVote.setCandidateID(candID);
        candidateName = candName;
        showVoteFrag();
    }

    private void setVotingTaskCompleted() {
        taskManager.onVoteCompleted();
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
        int numTasks;

        // must be called in onCreate()
        UITaskManager() {
            allBtns = new ArrayList<>();
            allLines = new ArrayList<>();
            LinearLayout ll = findViewById(R.id.task_layout);

            for (int i = 0; i < ll.getChildCount(); i++) {
                View view = ll.getChildAt(i);
                if (i % 2 == 0)
                    allBtns.add((PulsingButton) view);
                else {
                    allLines.add(view);
                }
            }
            numTasks = allBtns.size();
        }

        void startTasks() {
            allBtns.get(0).enablePulsingButton();
        }

        void initUI() {
            if(tasksCompleted == numTasks) {
                if(voteCompleted) {
                    onVoteCompleted();
                }
                else {
                    showSelectCandFrag();
                }
            }
            else {
                VoteUncomplFragment fragment = new VoteUncomplFragment();
                showFrag(fragment);
            }
            onTaskCompleted(tasksCompleted);
        }

        void onVoteCompleted() {
            voteCompleted = true;
            VoteComplFragment fragment = new VoteComplFragment();
            showFrag(fragment);
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

    private void showFrag(Fragment frag) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.candidate_task_container, frag);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    //////////////////////////////////////////////////////////////
    ////////////////////////////QR CODE///////////////////////////
    //////////////////////////////////////////////////////////////

    public void scanQRCode(View view) {
        Intent intent = new Intent(this, QRCodeActivity.class);

        startActivityForResult(intent, QR_ACTIVITY_REQ_CODE);
    }



    //////////////////////////////////////////////////////////////
    ////////////////////////////VOTING////////////////////////////
    //////////////////////////////////////////////////////////////

    private void showSelectCandFrag() {
        CandidateSelFragment candidateSelFragment = new CandidateSelFragment();
        showFrag(candidateSelFragment);
    }

    private void showVoteFrag() {
        VoteFragment voteFragment = new VoteFragment();
        showFrag(voteFragment);
    }

    public static class VoteComplFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_vote_compl, container, false);
        }
    }

    public static class VoteUncomplFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_vote_not_compl, container, false);
        }
    }

    public static class VoteFragment extends Fragment {
        Button vote;
        Button cancel;
        TextView confirmation;
        MainActivity myActivity;
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            final View fragment = inflater.inflate(R.layout.fragment_vote, container, false);
            vote = fragment.findViewById(R.id.candidate_vote_btn);
            cancel = fragment.findViewById(R.id.candidate_cancel_btn);
            confirmation = fragment.findViewById(R.id.vote_confirmation_txt);
            myActivity = (MainActivity) getActivity();

            String confStr = "Are you sure you want to vote for " + candidateName + "?";
            confirmation.setText(confStr);

            vote.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    myActivity.submitVote();
                }
            });
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    myActivity.showSelectCandFrag();
                }
            });
            return fragment;
        }
    }

    private void submitVote() {
        if (!myVote.checkAllFieldsExist()) {
            Toast.makeText(getApplicationContext(), "please complete all steps", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestQueue queue = Volley.newRequestQueue(this);

        String encryptedCandidateID = Utils.encryptVote(Integer.valueOf(myVote.getCandidateID()));

        String url = VOTING_URL + "REQUESTTYPE=VOTEREQUEST&" + COMM_QR_CODE + "=" + myVote.getQrCode() + "&"
                + COMM_NFC_ID + "=" + myVote.getNfcID() + "&"
                + COMM_CANDIDATE_ID + "=" + encryptedCandidateID;
        Log.v(TAG, "url is: " + url);

        String cleanurl = VOTING_URL + COMM_NFC_ID + "=" + myVote.getNfcID();

        JsonObject json = null;
        int retryCounter = 0;

        try{
            json = Ion.with(getApplicationContext()).load(url).asJsonObject().get();
            Log.v(TAG, "output is: " + json.toString());

            while(json.get("RESPONSETYPE").toString().equals("\"NULL\"")){
                sleep(100);
                json = Ion.with(getApplicationContext()).load(cleanurl).asJsonObject().get();
                Log.v(TAG, "output is: " + json.toString());

                retryCounter++;
                Log.v(TAG, "count:" + retryCounter);

                if(retryCounter > 5){
                    Log.v(TAG, "Retried 5 times!");
                    throw new RuntimeException();
                }
            }
        } catch (Exception e){
            Toast.makeText(getApplicationContext(), "Error getting response from server!", Toast.LENGTH_SHORT).show();
        }

        if(json != null){
            try {
                String configResponse = json.get("VOTEACCEPTED").toString();
                Log.v(TAG, "acceptstring is: " + configResponse);

                if (configResponse.equals("\"T\"")) {
                    Toast.makeText(getApplicationContext(), "Vote Confirmed!", Toast.LENGTH_SHORT).show();
                    // UI
                    setVotingTaskCompleted();
                } else {
                    String rejectionReason = json.get("REJECTIONMESSAGE").toString();
                    Toast.makeText(getApplicationContext(), "Vote Denied! " + rejectionReason, Toast.LENGTH_SHORT).show();
                }
            }
            catch(Exception e){
                Toast.makeText(getApplicationContext(), "Error getting response from server!", Toast.LENGTH_SHORT).show();
            }
        } else{
            Toast.makeText(getApplicationContext(), "Error getting response from server!", Toast.LENGTH_SHORT).show();
        }
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

    private void savePassportInfoToPref(String docNumStr, String birthDateStr, String expiryDateStr) {
        SharedPreferences sharedPref = this
                .getSharedPreferences(getString(R.string.pref_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.shared_pref_doc_num), docNumStr);
        editor.putString(getString(R.string.shared_pref_birthdate), birthDateStr);
        editor.putString(getString(R.string.shared_pref_expirydate), expiryDateStr);
        editor.apply(); //asynchronously save to pref
        Log.v(TAG, "saved to pref");
    }

    public static class PassportInfoPromptFragment extends DialogFragment implements View.OnClickListener {
        private EditText birthDate;
        private EditText expiryDate;
        private EditText docNum;
        private Button okBtn;

        private LinearLayout infoLayout;
        private LinearLayout promptLayout;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View fragment = inflater.inflate(R.layout.fragment_passport_info, container, false);
            infoLayout = fragment.findViewById(R.id.passport_info_layout);
            promptLayout = fragment.findViewById(R.id.passport_scan_prompt_layout);
            promptLayout.setVisibility(View.GONE);

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

                    Utils.savePassportInfoToPref(getContext(), docNumStr, birthDateStr, expiryDateStr);
//                    ((MainActivity) getActivity())
//                            .savePassportInfoToPref(docNumStr, birthDateStr, expiryDateStr);
                    infoLayout.setVisibility(View.INVISIBLE);
                    promptLayout.setVisibility(View.VISIBLE);
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