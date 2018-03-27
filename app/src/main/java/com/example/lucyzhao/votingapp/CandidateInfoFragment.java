package com.example.lucyzhao.votingapp;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import static com.example.lucyzhao.votingapp.Utils.COMM_CAND1_FN;
import static com.example.lucyzhao.votingapp.Utils.COMM_CAND1_LN;
import static com.example.lucyzhao.votingapp.Utils.COMM_CAND2_FN;
import static com.example.lucyzhao.votingapp.Utils.COMM_CAND2_LN;
import static com.example.lucyzhao.votingapp.Utils.VOTING_URL;

public class CandidateInfoFragment extends Fragment {
    private static final String TAG = CandidateInfoFragment.class.getSimpleName();
    private Button okBtn;

    private EditText cand1LastName;
    private EditText cand1FirstName;
    private EditText cand2LastName;
    private EditText cand2FirstName;


    public CandidateInfoFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View fragment = inflater.inflate(R.layout.fragment_candidate_info, container, false);

        okBtn = fragment.findViewById(R.id.candidate_config_ok_btn);

        cand1LastName = fragment.findViewById(R.id.cand1ln);
        cand1FirstName = fragment.findViewById(R.id.cand1fn);
        cand2LastName = fragment.findViewById(R.id.cand2ln);
        cand2FirstName = fragment.findViewById(R.id.cand2fn);

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
                    Toast.makeText(fragment.getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                }
                Log.v(TAG, cand1FirstNameStr);
            }
        });
        return fragment;
    }



    private void sendConfig(String cand1LN, String cand1FN, String cand2LN, String cand2FN) {
        RequestQueue queue = Volley.newRequestQueue(getContext());

        String url = VOTING_URL
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


    private boolean checkFields(String s1, String s2, String s3, String s4) {
        if (s1.isEmpty() || s2.isEmpty() || s3.isEmpty() || s4.isEmpty()) {
            return false;
        } else return true;
    }
}
