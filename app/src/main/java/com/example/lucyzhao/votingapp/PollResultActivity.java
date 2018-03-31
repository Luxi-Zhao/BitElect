package com.example.lucyzhao.votingapp;

import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class PollResultActivity extends NavActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.onCreateDrawer(R.layout.activity_poll_result);
        setTypeFace();
    }

    private void setTypeFace() {
        Typeface typeface = getResources().getFont(R.font.quicksand);

        TextView txt = findViewById(R.id.poll_result_title_txt);
        txt.setTypeface(typeface);
    }
}
