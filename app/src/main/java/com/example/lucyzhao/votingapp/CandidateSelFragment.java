package com.example.lucyzhao.votingapp;


import android.app.DialogFragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

import org.jmrtd.Util;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

public class CandidateSelFragment extends DialogFragment {
    private static final String TAG = CandidateSelFragment.class.getSimpleName();
    private static final String CAND_INFO_URL = "https://api.github.com/users/";

    private RadioGroup radioGroup;
    private Button okBtn;
    private ImageView testImg;
    private TextView testBio;
    private RadioButton testRadioBtn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View fragment = inflater.inflate(R.layout.fragment_candidate_sel, container, false);
        radioGroup = fragment.findViewById(R.id.radio_group);
        testImg = fragment.findViewById(R.id.testimg);
        testBio = fragment.findViewById(R.id.text1);
        testRadioBtn = fragment.findViewById(R.id.radio_clinton);

        okBtn = fragment.findViewById(R.id.candidate_sel_ok_btn);
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishCandSelTask(fragment);
            }
        });

        getCandidateProfile("bfbachmann", "1");

        return fragment;
    }

    private void finishCandSelTask(View fragment) {
        String candID = getSelectedCandidateID();
        String candName = getSelectedCandidateName(fragment);
        Log.v(TAG, "cand id is" + candID);
        if(candID.isEmpty()) {
            Toast.makeText(getContext(), "Please choose a candidate!", Toast.LENGTH_SHORT).show();
        }
        else {
            ((MainActivity) getActivity())
                    .setCandSelTaskCompleted(candID, candName);
            dismiss();
        }
    }

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

    private String getSelectedCandidateName(View fragment) {
        int id = radioGroup.getCheckedRadioButtonId();
        RadioButton selectedCand = fragment.findViewById(id);

        // if no item is chosen, this field would be null
        if (selectedCand == null)
            return "";
        else
            return selectedCand.getText().toString();
    }


    private void getCandidateProfile(String candUserName, String candID) {
        RequestQueue queue = Volley.newRequestQueue(getContext());

        String url = CAND_INFO_URL + candUserName;
        Log.v(TAG, "url is: " + url);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.v(TAG, response);
                        processResponse(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v(TAG, error.toString());
            }
        });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    /**
     * Updates bio field and starts getting profile image
     * @param response
     */
    private void processResponse(String response) {
        JSONObject obj = null;
        try {
            obj = new JSONObject(response);
            String bio = obj.getString(Utils.BIO);
            if(!bio.equals("null")) {
                testBio.setText(bio);
                Log.v(TAG, "bio is" + bio);
            }

            String profile_url = obj.getString(Utils.AVATAR_URL);
            new ProfilePicTask().execute(profile_url);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    private class ProfilePicTask extends AsyncTask<String, Void, Bitmap> {
        protected Bitmap doInBackground(String... params) {
            try {
                String urlStr = params[0];
                java.net.URL url = new java.net.URL(urlStr);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                return BitmapFactory.decodeStream(input);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        /**
         * Display profile image on screen
         * @param b
         */
        protected void onPostExecute(Bitmap b) {
            if(b != null) {
               // testImg.setImageBitmap(b);
                BitmapDrawable bd = new BitmapDrawable(getResources(), b);
                testRadioBtn.setBackground(bd);
            }
        }
    }


}
