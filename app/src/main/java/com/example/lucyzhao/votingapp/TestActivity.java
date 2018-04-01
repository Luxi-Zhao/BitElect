package com.example.lucyzhao.votingapp;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.FaceDetector;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import static com.example.lucyzhao.votingapp.FaceComparer.compareImgs;
import static com.example.lucyzhao.votingapp.FaceComparer.compareImgsRec;
import static com.example.lucyzhao.votingapp.FaceComparer.greySrc;
import static com.example.lucyzhao.votingapp.FaceComparer.greyTar;
import static com.example.lucyzhao.votingapp.FaceComparer.maxSrc;

public class TestActivity extends AppCompatActivity {

    String TAG = TestActivity.class.getSimpleName();

    ImageView img1;
    ImageView img2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        img1 = findViewById(R.id.img1);
        img2 = findViewById(R.id.img2);


        Log.v(TAG, "in test activity on create");
        Bitmap b1 = Utils.retrieveImg(false, "0", "0", this);
        Bitmap b2 = Utils.retrieveImg(true, Utils.YOUR_FACE_ID, "1", this);

        if(b1 == null || b2 == null || FaceComparer.greyTar == null || FaceComparer.greySrc == null) return;
        float ret = compareImgs(b1, b2);
        img1.setImageBitmap(FaceComparer.greySrc);
        img2.setImageBitmap(FaceComparer.greyTar);
        Log.v(TAG, "similarity ratio is " + ret);
        TextView tv = findViewById(R.id.ratio);
        String s = "Similarity ratio is: " + Float.toString(ret);
        tv.setText(s);
    }


}
