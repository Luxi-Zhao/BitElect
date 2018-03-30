package com.example.lucyzhao.votingapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import static com.example.lucyzhao.votingapp.Utils.COMM_CAND1_FN;
import static com.example.lucyzhao.votingapp.Utils.COMM_CAND1_LN;
import static com.example.lucyzhao.votingapp.Utils.COMM_CAND2_FN;
import static com.example.lucyzhao.votingapp.Utils.COMM_CAND2_LN;
import static com.example.lucyzhao.votingapp.Utils.COMM_NFC_ID;
import static com.example.lucyzhao.votingapp.Utils.VOTING_URL;

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

        if (checkFields(cand1FirstNameStr, cand1LastNameStr, cand2FirstNameStr, cand2LastNameStr)) {
            sendConfig(cand1LastNameStr, cand1FirstNameStr, cand2LastNameStr, cand2FirstNameStr);
        } else {
            Toast.makeText(getApplicationContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
        }
        Log.v(TAG, cand1FirstNameStr);
    }

    private void sendConfig(String cand1LN, String cand1FN, String cand2LN, String cand2FN) {
        RequestQueue queue = Volley.newRequestQueue(this);

        String url = VOTING_URL
                + "REQUESTTYPE=CONFIGREQUEST&"
                + COMM_NFC_ID + "=" + "1234" + "&"
                + COMM_CAND1_FN + "=" + cand1FN + "&"
                + COMM_CAND1_LN + "=" + cand1LN + "&"
                + COMM_CAND2_FN + "=" + cand2FN + "&"
                + COMM_CAND2_LN + "=" + cand2LN;

        Log.v(TAG, "url is: " + url);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.v(TAG,"RESPONSE:"+response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //No Response
            }
        });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);

        url = VOTING_URL;

        // Request a string response from the provided URL.
        StringRequest infoRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.v(TAG,"RESPONSE:"+response);
                        readResponse(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v(TAG,"ERROR" + error.toString());

            }
        });

        // Add the request to the RequestQueue.
        queue.add(infoRequest);
    }


    private void readResponse(String response){
        try{
            JSONObject jsonObj = new JSONObject(response);
            String configResponse = jsonObj.getString("CONFIGACCEPTED");

            Toast.makeText(getApplicationContext(), "Response:" + configResponse, Toast.LENGTH_SHORT).show();
        } catch (JSONException e){
            Toast.makeText(getApplicationContext(), "Error getting response from server!", Toast.LENGTH_SHORT).show();
        }

    }

    private boolean checkFields(String s1, String s2, String s3, String s4) {
        if (s1.isEmpty() || s2.isEmpty() || s3.isEmpty() || s4.isEmpty()) {
            return false;
        } else return true;
    }
}
