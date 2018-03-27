package com.example.lucyzhao.votingapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class CandidateInfoActivity extends AppCompatActivity {
    private static final String TAG = CandidateInfoActivity.class.getSimpleName();
    private Button okBtn;

    private EditText cand1LastName;
    private EditText cand1FirstName;
    private EditText cand2LastName;
    private EditText cand2FirstName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_candidate_info);
        okBtn = findViewById(R.id.candidate_config_ok_btn);

        cand1LastName = findViewById(R.id.cand1ln);
        cand1FirstName = findViewById(R.id.cand1fn);
        cand2LastName = findViewById(R.id.cand2ln);
        cand2FirstName = findViewById(R.id.cand2fn);
    }

    public void submitConfig(View view) {
        String cand1LastNameStr = cand1LastName.getText().toString();
        String cand1FirstNameStr = cand1FirstName.getText().toString();
        String cand2LastNameStr = cand2LastName.getText().toString();
        String cand2FirstNameStr = cand2FirstName.getText().toString();

        if(checkFields(cand1FirstNameStr, cand1LastNameStr, cand2FirstNameStr, cand2LastNameStr)) {
            sendConfig();
        }
        else {
            Toast.makeText(getApplicationContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
        }
        Log.v(TAG, cand1FirstNameStr);
    }

    private void sendConfig() {

    }


    private boolean checkFields(String s1, String s2, String s3, String s4) {
        if(s1.isEmpty() || s2.isEmpty() || s3.isEmpty() || s4.isEmpty()) {
            return false;
        }
        else return true;
    }
}
