package com.example.lucyzhao.votingapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class PollResultActivity extends NavActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.onCreateDrawer(R.layout.activity_poll_result);
    }
}
