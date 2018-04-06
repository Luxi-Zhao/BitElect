package com.example.lucyzhao.votingapp.poll_results;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lucyzhao.votingapp.JSONReq;
import com.example.lucyzhao.votingapp.NavActivity;
import com.example.lucyzhao.votingapp.R;
import com.example.lucyzhao.votingapp.Utils;

import java.lang.ref.WeakReference;

public class PollResultActivity extends NavActivity {
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.onCreateDrawer(R.layout.activity_poll_result);
        Utils.setTypeFace(this, R.id.poll_result_title_txt, R.id.poll_result_valid);

        progressBar = findViewById(R.id.poll_result_pb);

        String nfcID = Utils.getDocNum(this);
        if (nfcID.equals("")) {
            Toast.makeText(this, R.string.navigation_drawer_docnum_none, Toast.LENGTH_LONG).show();
            return;
        }
        new GetPollResultTask(this).execute(nfcID);
    }

    private static class GetPollResultTask extends AsyncTask<String, Void, String[]> {
        WeakReference<PollResultActivity> activityRef;

        GetPollResultTask(PollResultActivity activity) {
            this.activityRef = new WeakReference<>(activity);
        }

        @Override
        protected String[] doInBackground(String... params) {
            if (activityRef.get() == null || activityRef.get().isFinishing())
                return null;

            // getting poll results
            String nfcID = params[0];
            return JSONReq.getPollResult(activityRef.get(), nfcID);
        }

        @Override
        protected void onPostExecute(String[] pollResults) {
            PollResultActivity activity = activityRef.get();
            if(activity == null || activity.isFinishing()) {
                return;
            }
            activity.progressBar.setVisibility(View.GONE);

            if (pollResults == null) {
                Toast.makeText(activity.getApplicationContext(), R.string.poll_result_getting_err, Toast.LENGTH_LONG).show();
                return;
            }

            // displaying the poll
            String valid = pollResults[0];
            String cand1fn = pollResults[1];
            String cand1ln = pollResults[2];
            String cand2fn = pollResults[3];
            String cand2ln = pollResults[4];
            String cand1Votes = pollResults[5];
            String cand2Votes = pollResults[6];
            int cand1v = Integer.parseInt(cand1Votes);
            int cand2v = Integer.parseInt(cand2Votes);

            BarGraph barGraph = new BarGraph(activity.getApplicationContext());
            barGraph.setCandNames(cand1fn, cand1ln, cand2fn, cand2ln);
            barGraph.setPollResult(cand1v, cand2v);

            // start poll displaying
            RelativeLayout layout = activity.findViewById(R.id.poll_result_layout);

            barGraph.setLayoutParams(new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT));

            layout.addView(barGraph);


            // validity display
            TextView validTxt = activity.findViewById(R.id.poll_result_valid);
            if (valid.equals(Utils.TRUE)) {
                validTxt.setText(R.string.poll_result_valid);
            } else {
                validTxt.setText(R.string.poll_result_invalid);
            }

        }
    }
}
