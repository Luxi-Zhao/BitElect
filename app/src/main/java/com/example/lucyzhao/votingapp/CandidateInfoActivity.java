package com.example.lucyzhao.votingapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.koushikdutta.ion.Ion;

import static com.example.lucyzhao.votingapp.Utils.COMM_CAND1_FN;
import static com.example.lucyzhao.votingapp.Utils.COMM_CAND1_LN;
import static com.example.lucyzhao.votingapp.Utils.COMM_CAND2_FN;
import static com.example.lucyzhao.votingapp.Utils.COMM_CAND2_LN;
import static com.example.lucyzhao.votingapp.Utils.COMM_NFC_ID;
import static com.example.lucyzhao.votingapp.Utils.VOTING_URL;
import static java.lang.Thread.sleep;

public class CandidateInfoActivity extends NavActivity {
    private static final String TAG = CandidateInfoActivity.class.getSimpleName();
    private Button okBtn;

    private EditText cand1LastName;
    private EditText cand1FirstName;
    private EditText cand2LastName;
    private EditText cand2FirstName;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.onCreateDrawer(R.layout.activity_candidate_info);
        Utils.setTypeFace(this, R.id.cand_config_title_txt);

        okBtn = findViewById(R.id.candidate_config_ok_btn);

        cand1LastName = findViewById(R.id.cand1ln);
        cand1FirstName = findViewById(R.id.cand1fn);
        cand2LastName = findViewById(R.id.cand2ln);
        cand2FirstName = findViewById(R.id.cand2fn);

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String cand1LastNameStr = cand1LastName.getText().toString();
                String cand1FirstNameStr = cand1FirstName.getText().toString();
                String cand2LastNameStr = cand2LastName.getText().toString();
                String cand2FirstNameStr = cand2FirstName.getText().toString();

                if (checkFields(cand1FirstNameStr, cand1LastNameStr, cand2FirstNameStr, cand2LastNameStr)) {
                    sendConfig(cand1LastNameStr, cand1FirstNameStr, cand2LastNameStr, cand2FirstNameStr);
                } else {
                    Toast.makeText(getApplicationContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                }
                Log.v(TAG, cand1FirstNameStr);
            }
        });
    }

    private void sendConfig(String cand1LN, String cand1FN, String cand2LN, String cand2FN) {
        String url = VOTING_URL
                + "REQUESTTYPE=CONFIGREQUEST&"
                + COMM_NFC_ID + "=" + "1234" + "&"
                + COMM_CAND1_FN + "=" + cand1FN + "&"
                + COMM_CAND1_LN + "=" + cand1LN + "&"
                + COMM_CAND2_FN + "=" + cand2FN + "&"
                + COMM_CAND2_LN + "=" + cand2LN;

        String cleanurl = VOTING_URL + COMM_NFC_ID + "=" + "1234";
        Log.v(TAG, "url is: " + url);

        JsonObject json = null;

        int retryCounter = 0;

        try {
            json = Ion.with(getApplicationContext()).load(url).asJsonObject().get();
            Log.v(TAG, "output is: " + json.toString());

            while (json.get("RESPONSETYPE").toString().equals("\"NULL\"")) {
                sleep(100);
                json = Ion.with(getApplicationContext()).load(cleanurl).asJsonObject().get();
                Log.v(TAG, "output is: " + json.toString());

                retryCounter++;
                Log.v(TAG, "count:" + retryCounter);

                if (retryCounter > 5) {
                    Log.v(TAG, "Retried 5 times!");
                    throw new RuntimeException();
                }
            }
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Error getting response from server!", Toast.LENGTH_SHORT).show();
        }

        if (json != null) {
            try {
                String configResponse = json.get("CONFIGACCEPTED").toString();
                Log.v(TAG, "configacc is: " + configResponse);

                if (configResponse.equals("\"T\"")) {
                    Toast.makeText(getApplicationContext(), "Configuration Successful!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Configuration Unsuccessful!", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "Error getting response from server!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Error getting response from server!", Toast.LENGTH_SHORT).show();
        }

    }


    private boolean checkFields(String s1, String s2, String s3, String s4) {
        if (s1.isEmpty() || s2.isEmpty() || s3.isEmpty() || s4.isEmpty()) {
            return false;
        } else return true;
    }
}
