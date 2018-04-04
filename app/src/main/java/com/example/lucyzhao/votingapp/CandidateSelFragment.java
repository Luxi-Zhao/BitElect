package com.example.lucyzhao.votingapp;


import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

public class CandidateSelFragment extends Fragment {
    private static final String TAG = CandidateSelFragment.class.getSimpleName();
    private static final String CAND_INFO_URL = "https://api.github.com/users/";

    private RadioGroup radioGroup;
    private Button okBtn;
    private RadioButton radioCand1, radioCand2;

    private List<ImageView> candImgs = new ArrayList<>();
    private List<TextView> candBios = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View fragment = inflater.inflate(R.layout.fragment_candidate_sel, container, false);
        radioGroup = fragment.findViewById(R.id.radio_group);
        ImageView cand1Img = fragment.findViewById(R.id.cand1Img);
        TextView cand1Bio = fragment.findViewById(R.id.cand1Info);
        ImageView cand2Img = fragment.findViewById(R.id.cand2Img);
        TextView cand2Bio = fragment.findViewById(R.id.cand2Info);
        radioCand1 = fragment.findViewById(R.id.radio_cand1);
        radioCand2 = fragment.findViewById(R.id.radio_cand2);

        candImgs.add(cand1Img);
        candImgs.add(cand2Img);
        candBios.add(cand1Bio);
        candBios.add(cand2Bio);


        okBtn = fragment.findViewById(R.id.candidate_sel_ok_btn);
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishCandSelTask(fragment);
            }
        });

        String nfcID = Utils.getDocNum(getContext());
        if(nfcID.equals("")) {
            Toast.makeText(getContext(), R.string.navigation_drawer_docnum_none, Toast.LENGTH_SHORT).show();
        }
        else {
            new GetCandNamesTask(this).execute(nfcID);
        }

//        getCandidateProfile("peterdeutsch", 1);
//        getCandidateProfile("Luxi-Zhao", 2);

        return fragment;
    }

    private static class GetCandNamesTask extends AsyncTask<String, Void, String[]> {
        WeakReference<CandidateSelFragment> fragmentRef;

        GetCandNamesTask(CandidateSelFragment fragment) {
            fragmentRef = new WeakReference<>(fragment);
        }

        @Override
        protected String[] doInBackground(String... params) {
            CandidateSelFragment fragment = fragmentRef.get();
            if(fragment == null || fragment.isDetached() || fragment.isRemoving()) {
                return null;
            }

            String nfcID = params[0];
            String[] results = JSONReq.getPollResult(fragment.getContext(), nfcID);
            return results;
        }

        @Override
        protected void onPostExecute(String[] pollResults) {
            final CandidateSelFragment fragment = fragmentRef.get();
            if(fragment == null || fragment.isDetached() || fragment.isRemoving()) {
                return;
            }

            if(pollResults == null) {
                Toast.makeText(fragment.getContext(), R.string.cand_names_getting_err, Toast.LENGTH_SHORT).show();
                return;
            }

            String cand1fn = pollResults[1];
            String cand1ln = pollResults[2];
            String cand2fn = pollResults[3];
            String cand2ln = pollResults[4];
            String cand1Name = cand1fn + " " + cand1ln;
            String cand2Name = cand2fn + " " + cand2ln;

            Log.v(TAG, "cand1 name "  + cand1Name + " cand2 name is " + cand2Name);

            fragment.radioCand1.setText(cand1Name);
            fragment.radioCand2.setText(cand2Name);

            fragment.getCandidateProfile("peterdeutsch", 1);
            fragment.getCandidateProfile("Luxi-Zhao", 2);
        }
    }

    private void finishCandSelTask(View fragment) {
        String candID = getSelectedCandidateID();
        String candName = getSelectedCandidateName(fragment);
        Log.v(TAG, "cand id is" + candID);
        if (candID.isEmpty()) {
            Toast.makeText(getContext(), "Please choose a candidate!", Toast.LENGTH_SHORT).show();
        } else {
            ((MainActivity) getActivity())
                    .setCandSelTaskCompleted(candID, candName);
        }
    }

    private String getSelectedCandidateID() {
        String candidateID = "";
        int id = radioGroup.getCheckedRadioButtonId();
        // Check which radio button was clicked
        switch (id) {
            case R.id.radio_cand1:
                candidateID = "1";
                break;

            case R.id.radio_cand2:
                candidateID = "2";
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


    private void getCandidateProfile(String candUserName, final int candID) {
        RequestQueue queue = Volley.newRequestQueue(getContext());

        String url = CAND_INFO_URL + candUserName;
        Log.v(TAG, "url is: " + url);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.v(TAG, response);
                        processResponse(response, candID);
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
     *
     * @param response
     * @param candID   limited to 1 or 2
     */
    private void processResponse(String response, int candID) {
        JSONObject obj;
        try {
            obj = new JSONObject(response);
            String bio = obj.getString(Utils.BIO);
            if (!bio.equals("null")) {
                candBios.get(candID - 1).setText(bio);
                Log.v(TAG, "CAND " + candID + " bio is" + bio);
            }

            String profile_url = obj.getString(Utils.AVATAR_URL);
            new ProfilePicTask(candID, this).execute(profile_url);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    private static class ProfilePicTask extends AsyncTask<String, Void, Bitmap> {
        final int candID;
        WeakReference<CandidateSelFragment> fragmentRef;

        ProfilePicTask(int candID, CandidateSelFragment fragment) {
            this.candID = candID;
            fragmentRef = new WeakReference<>(fragment);
        }

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
         *
         * @param b
         */
        protected void onPostExecute(Bitmap b) {
            // check if fragment is attached to activity
            CandidateSelFragment fragment = fragmentRef.get();
            if (fragment == null || fragment.isDetached() || fragment.isRemoving()) {
                return;
            }

            if (b != null && fragment.isAdded()) {
                // cand1Img.setImageBitmap(b);
                BitmapDrawable bd = new BitmapDrawable(fragment.getResources(), b);
                fragment.candImgs.get(candID - 1).setBackground(bd);
            } else {
                Log.v(TAG, "profile img view is null");
            }
        }
    }


}
