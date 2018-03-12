package com.example.lucyzhao.votingapp;

import android.Manifest;
import android.app.Activity;
import com.example.lucyzhao.votingapp.Utils;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.transition.Slide;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static com.example.lucyzhao.votingapp.Utils.COMM_CANDIDATE_ID;
import static com.example.lucyzhao.votingapp.Utils.COMM_NFC_ID;
import static com.example.lucyzhao.votingapp.Utils.COMM_QR_CODE;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int QR_ACTIVITY_REQ_CODE = 111;

    TextView myTextView;
    AlertDialog.Builder builder;
    private VoteInfo voteInfo = new VoteInfo();

    private class VoteInfo {
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

        boolean checkAllFieldsExist() {
            return !nfcID.equals("") && !qrCode.equals("") && !candidateID.equals("");
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myTextView = findViewById(R.id.qr_code_txt);
        builder = new AlertDialog.Builder(this);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                submitVote();
            }
        });

//        // check QR Code scanning result
        Intent intent = this.getIntent();
//        String qr_result = intent.getStringExtra(Utils.QR_RESULT);
//        if (qr_result != null) {
//            Log.v(TAG, "result of qr scanning is:" + qr_result);
//            myTextView.setText(qr_result);
//            voteInfo.setQrCode(qr_result);
//        } else {
//            Log.v(TAG, "qr scanning returned null!");
//        }

        // check NFC scanning result
        String nfc_result = intent.getStringExtra(Utils.NFC_RESULT);
        if (nfc_result != null) {
            Log.v(TAG, "result of nfc scanning is:" + nfc_result);
            voteInfo.setNfcID(nfc_result);
        } else {
            Log.v(TAG, "nfc scanning returned null!");
        }


    }


    public void scanQRCode(View view) {
        Intent intent = new Intent(this, QRCodeActivity.class);
        startActivityForResult(intent, QR_ACTIVITY_REQ_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case (QR_ACTIVITY_REQ_CODE) : {
                if (resultCode == Activity.RESULT_OK) {
                    // check QR Code scanning result
                    String qr_result = data.getStringExtra(Utils.QR_RESULT);
                    if (qr_result != null) {
                        Log.v(TAG, "result of qr scanning is:" + qr_result);
                        myTextView.setText(qr_result);
                        voteInfo.setQrCode(qr_result);
                    } else {
                        Log.v(TAG, "qr scanning returned null!");
                    }
                }
                break;
            }

//            case (NFC_ACTIVITY_REQ_CODE) : {
//                if (resultCode == Activity.RESULT_OK) {
//                    String nfc_result = data.getStringExtra(Utils.NFC_RESULT);
//                    if (nfc_result != null) {
//                        Log.v(TAG, "result of nfc scanning is:" + nfc_result);
//                        voteInfo.setNfcID(nfc_result);
//                    } else {
//                        Log.v(TAG, "nfc scanning returned null!");
//                    }
//                }
//                break;
//            }
        }
    }

    public void enterPassportInfo(View view) {
        new PassportInfoFragment().show(getFragmentManager(), "enterPassportInfo");
    }

    public void vote(View view) {
        String candidate = "";
        //startActivity(new Intent(this, VotingActivity.class));
        // Is the button now checked?
        RadioGroup rg = findViewById(R.id.radio_group);
        int id = rg.getCheckedRadioButtonId();


        // Check which radio button was clicked
        switch(id) {
            case R.id.radio_clinton:
                candidate = "1";

                break;
            case R.id.radio_trump:
                candidate = "2";
                break;

            case R.id.radio_someoneelse:
                candidate = "3";
                break;
        }

        voteInfo.setCandidateID(candidate);
        RadioButton selectedCand = findViewById(id);
        String candidateName = candidate;
        if(selectedCand != null) {
            candidateName = selectedCand.getText().toString();
        }

        builder.setMessage("Are you sure you want to choose " + candidateName +"?");
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void submitVote() {

        if(!voteInfo.checkAllFieldsExist()) {
            Toast.makeText(getApplicationContext(), "please complete all steps", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestQueue queue = Volley.newRequestQueue(this);

        String url = "http://192.168.4.1/?" + COMM_QR_CODE + "=" + voteInfo.qrCode + "&"
                + COMM_NFC_ID + "=" + voteInfo.nfcID + "&"
                + COMM_CANDIDATE_ID + "=" + voteInfo.candidateID;
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
    }


    public static class PassportInfoFragment extends DialogFragment implements View.OnClickListener {
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

                    SharedPreferences sharedPref = getActivity()
                            .getSharedPreferences(getString(R.string.pref_file_key), Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString(getString(R.string.shared_pref_doc_num), docNumStr);
                    editor.putString(getString(R.string.shared_pref_birthdate), birthDateStr);
                    editor.putString(getString(R.string.shared_pref_expirydate), expiryDateStr);
                    editor.apply(); //asynchronously save to pref
                    Log.v(TAG, "saved to pref");
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