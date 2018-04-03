package com.example.lucyzhao.votingapp;

import android.os.Bundle;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class PollResultActivity extends NavActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.onCreateDrawer(R.layout.activity_poll_result);
        Utils.setTypeFace(this, R.id.poll_result_title_txt, R.id.poll_result_valid);

        String nfcID = Utils.getDocNum(this);
        if (nfcID.equals("")) {
            Toast.makeText(this, R.string.navigation_drawer_docnum_none, Toast.LENGTH_LONG).show();
            return;
        }

        // getting poll results
        String[] pollResults = JSONReq.getPollResult(this, nfcID);
        if (pollResults == null) {
            Toast.makeText(this, R.string.poll_result_getting_err, Toast.LENGTH_LONG).show();
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

        //todo process results if there are quotes around them
//            ////////////////
//            cand1fn = "cand1fn";
//            cand1ln="cand1ln";
//            cand2fn= "cand2fn";
//            cand2ln="cand2ln";
//            int cand2v = 27;
//            int cand1v = 14;
//            ////////////////

        BarGraph barGraph = new BarGraph(this);
        barGraph.setCandNames(cand1fn, cand1ln, cand2fn, cand2ln);
        barGraph.setPollResult(cand1v, cand2v);

        // start poll displaying
        RelativeLayout layout = findViewById(R.id.poll_result_layout);

        barGraph.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT));

        layout.addView(barGraph);


        // validity display
        TextView validTxt = findViewById(R.id.poll_result_valid);
        if (valid.equals(Utils.TRUE)) {
            validTxt.setText(R.string.poll_result_valid);
        } else {
            validTxt.setText(R.string.poll_result_invalid);
        }


    }
}
