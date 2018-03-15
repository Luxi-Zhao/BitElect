package com.example.lucyzhao.votingapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import static com.example.lucyzhao.votingapp.Utils.COMM_CANDIDATE_ID;
import static com.example.lucyzhao.votingapp.Utils.COMM_NFC_ID;
import static com.example.lucyzhao.votingapp.Utils.COMM_QR_CODE;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int QR_ACTIVITY_REQ_CODE = 111;
    private static final String VOTING_URL = "http://192.168.4.1/?";

    private static final int STROKE_WIDTH = 5;
    private PulsatingButton enterPassportInfoBtn;
    private PulsatingButton promptPassportScanBtn;
    private PulsatingButton scanQRBtn;
    private RadioGroup radioGroup;
    private PulsatingButton voteBtn;

    AlertDialog.Builder votingAlertBuilder;
    private Vote myVote = Vote.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // UI elements
        enterPassportInfoBtn = findViewById(R.id.enter_passport_info_btn);
        promptPassportScanBtn = findViewById(R.id.scan_passport_prompt_btn);
        scanQRBtn = findViewById(R.id.scan_qr_btn);
        radioGroup = findViewById(R.id.radio_group);
        voteBtn = findViewById(R.id.vote_btn);

        votingAlertBuilder = new AlertDialog.Builder(this);
        votingAlertBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                submitVote();
            }
        });

        // check NFC scanning result
        String nfc_result = getIntent().getStringExtra(Utils.NFC_RESULT);
        if (nfc_result != null) {
            Log.v(TAG, "result of nfc scanning is:" + nfc_result);
            //myVote.setNfcID(nfc_result);
            setPassportScanTaskCompleted(nfc_result);
        } else {
            Log.v(TAG, "nfc scanning not performed");
            enterPassportInfoBtn.startAnimation();
        }

    }

    private void setEnterPassportInfoTaskCompleted(String docNumStr, String birthDateStr, String expiryDateStr) {
        SharedPreferences sharedPref = this
                .getSharedPreferences(getString(R.string.pref_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.shared_pref_doc_num), docNumStr);
        editor.putString(getString(R.string.shared_pref_birthdate), birthDateStr);
        editor.putString(getString(R.string.shared_pref_expirydate), expiryDateStr);
        editor.apply(); //asynchronously save to pref
        Log.v(TAG, "saved to pref");

        enterPassportInfoBtn.setCompleted();
        promptPassportScanBtn.startAnimation();
    }

    private void setPassportScanTaskCompleted(String nfc_result) {
        myVote.setNfcID(nfc_result);

        View vLine = findViewById(R.id.line_1);
        vLine.setBackgroundColor(ContextCompat.getColor(this, R.color.colorBtnCompleted));

        enterPassportInfoBtn.setCompleted();
        promptPassportScanBtn.setCompleted();
        scanQRBtn.startAnimation();
    }

    private void setQRTaskCompleted(String qr_result) {
        myVote.setQrCode(qr_result);

        View vLine = findViewById(R.id.line_2);
        vLine.setBackgroundColor(ContextCompat.getColor(this, R.color.colorBtnCompleted));

        enterPassportInfoBtn.setCompleted();
        promptPassportScanBtn.setCompleted();
        scanQRBtn.setCompleted();
    }

    private void setCandSelTaskCompleted() {
        GradientDrawable bg = (GradientDrawable) radioGroup.getBackground().getCurrent();
        bg.setStroke(STROKE_WIDTH, ContextCompat.getColor(this, R.color.colorBtnCompleted));
        View vLine = findViewById(R.id.line_3);
        vLine.setBackgroundColor(ContextCompat.getColor(this, R.color.colorBtnCompleted));

        enterPassportInfoBtn.setCompleted();
        promptPassportScanBtn.setCompleted();
        scanQRBtn.setCompleted();
        voteBtn.startAnimation();
    }

    private void setVotingTaskCompleted() {
        enterPassportInfoBtn.setCompleted();
        promptPassportScanBtn.setCompleted();
        scanQRBtn.setCompleted();
        voteBtn.setCompleted();
    }



    //////////////////////////////////////////////////////////////
    ////////////////////////////QR CODE///////////////////////////
    //////////////////////////////////////////////////////////////

    public void scanQRCode(View view) {
        Intent intent = new Intent(this, QRCodeActivity.class);
        startActivityForResult(intent, QR_ACTIVITY_REQ_CODE);
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
                        //myVote.setQrCode(qr_result);
                        setQRTaskCompleted(qr_result);
                    } else {
                        Log.v(TAG, "qr scanning returned null!");
                    }
                }
                break;
            }

        }
    }






    //////////////////////////////////////////////////////////////
    ////////////////////////////VOTING////////////////////////////
    //////////////////////////////////////////////////////////////

    private String getSelectedCandidateID() {
        String candidateID = "";
        int id = radioGroup.getCheckedRadioButtonId();
        // Check which radio button was clicked
        switch (id) {
            case R.id.radio_clinton:
                candidateID = "1";

                break;
            case R.id.radio_trump:
                candidateID = "2";
                break;

            case R.id.radio_someoneelse:
                candidateID = "3";
                break;
        }
        return candidateID;
    }

    private String getSelectedCandidateName() {
        RadioGroup rg = findViewById(R.id.radio_group);
        int id = rg.getCheckedRadioButtonId();
        RadioButton selectedCand = findViewById(id);

        // if no item is chosen, this field would be null
        if(selectedCand == null)
            return "";
        else
            return selectedCand.getText().toString();
    }

    public void onCandidateChosen(View view) {
        setCandSelTaskCompleted();
    }

    public void vote(View view) {
        String candidateID = getSelectedCandidateID();
        myVote.setCandidateID(candidateID);

        votingAlertBuilder.setMessage("Are you sure you want to choose " + getSelectedCandidateName() + "?");
        AlertDialog dialog = votingAlertBuilder.create();
        dialog.show();
    }

    private void submitVote() {
        if (!myVote.checkAllFieldsExist()) {
            Toast.makeText(getApplicationContext(), "please complete all steps", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestQueue queue = Volley.newRequestQueue(this);

        String url = VOTING_URL + COMM_QR_CODE + "=" + myVote.getQrCode() + "&"
                + COMM_NFC_ID + "=" + myVote.getNfcID() + "&"
                + COMM_CANDIDATE_ID + "=" + myVote.getCandidateID();
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

                    ((MainActivity)getActivity())
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


}