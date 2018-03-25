package com.example.lucyzhao.votingapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class TestActivity extends AppCompatActivity {

    String TAG = TestActivity.class.getSimpleName();

    ImageView img1;
    ImageView img2;
    ImageView img3;
    ImageView img4;
    ImageView img5;
    ImageView img6;
    ImageView imgp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        img1 = findViewById(R.id.img1);
        img2 = findViewById(R.id.img2);
        img3 = findViewById(R.id.img3);
        img4 = findViewById(R.id.img4);
        img5 = findViewById(R.id.img5);
        img6 = findViewById(R.id.img6);
        imgp = findViewById(R.id.imgp);

        Log.v(TAG, "in test activity on create");
        Bitmap b1 = Utils.retrieveImg(false, "0", "0", this);
        Bitmap b2 = Utils.retrieveImg(true, "8", "1", this);
        img1.setImageBitmap(b1);
        img2.setImageBitmap(b2);
        float ret = imgComparison(b1, b2);
        Log.v(TAG, "similarity ratio is " + ret);
        TextView tv = findViewById(R.id.ratio);
        String s = Float.toString(ret);
        tv.setText(s);
    }

    /**
     *
     * @param target passport photo
     * @param source picture taken by camera
     * @return
     */
    private float imgComparison(Bitmap target, Bitmap source) {
        int smallH = Math.min(target.getHeight(), source.getHeight());
        int smallW = Math.min(target.getWidth(), source.getWidth());

        Bitmap scaledTarget = Bitmap.createScaledBitmap(target, smallW, smallH, false);
        Bitmap scaledSource = Bitmap.createScaledBitmap(source, smallW, smallH, false);

        Log.v(TAG, "target size: " + scaledTarget.getByteCount() + " source size " + scaledSource.getByteCount());

        Bitmap greyTarget = toGreyScale(scaledTarget);
        Bitmap greySource = toGreyScale(scaledSource);

        int faceColorTarget = greyTarget.getPixel((int)(greyTarget.getWidth()/3.0), (int)(greyTarget.getHeight()/2.0));
        //Bitmap greyFiltered = adjustFaceColor(greySource, faceColorTarget);

        img1.setImageBitmap(greyTarget);
        img2.setImageBitmap(greySource);

        int countEquals = 0;
        int numPixels = greySource.getWidth() * greySource.getHeight();
        for(int i = 0; i < greyTarget.getWidth(); i++ ) {
            for(int j = 0; j < greyTarget.getHeight(); j++) {
                int p1 = greySource.getPixel(i, j);
                int p2 = greyTarget.getPixel(i, j);
                int ret = pixelsAreEqual(p1, p2);
                if(ret == 1) {
                    countEquals++;
                }
                else if(ret == 2) {
                    numPixels--;
                }
            }
        }


        float percent = (float) countEquals / (float) numPixels;
        Log.v(TAG, "number of equals " + countEquals + " number of pixels " + greySource.getWidth() * greySource.getHeight());
        Log.v(TAG, "width * height " + greySource.getWidth() * greySource.getHeight());
        return percent * 100;

    }

    /**
     * Recycle bitmap passed in
     * @param bitmap
     * @return
     */
    private Bitmap toGreyScale(Bitmap bitmap) {
        int size = bitmap.getByteCount();
        int[] pixels = new int[size];
        int[] greyPs = new int[size];
        bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0,0, bitmap.getWidth(), bitmap.getHeight() );

        for(int i = 0; i < size; i++) {
            int p = pixels[i];
            int r = Color.red(p);
            int g = Color.green(p);
            int b = Color.blue(p);
            int avg = (r+g+b) / 3;
            greyPs[i] = Color.rgb(avg, avg, avg);
        }
        // each pixel stored as 4 byte
        Bitmap ret = Bitmap.createBitmap(greyPs, bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        ret = Bitmap.createBitmap(ret, 0, (int)(bitmap.getHeight()*(2.0/6.0)), bitmap.getWidth(), (int)(bitmap.getHeight()*(4.0/6.0)));
        bitmap.recycle();
        return ret;
    }

    private int pixelsAreEqual(int p1, int p2) {
        int r1 = Color.red(p1);
        int r2 = Color.red(p2);
        //Log.v(TAG, "pixel of img1 is: " + r1 + " pixel of img2 is: " + r2);
        if(r1 == 0 || r2 == 0) return 2;
        else if(Math.abs(p1 - p2) <= 10) return 1;
        else return 0;
    }

    /**
     * Recycles bitmap passed in
     * @param orig
     * @param refColor
     * @return
     */
    private Bitmap adjustFaceColor(Bitmap orig, int refColor) {
        Bitmap mutableB = orig.copy(Bitmap.Config.ARGB_8888, true);
        int faceColor = orig.getPixel((int)(orig.getWidth()/3.0), (int)(orig.getHeight()/2.0));
        for(int i = 0; i < orig.getWidth(); i++ ) {
            for(int j = 0; j < orig.getHeight(); j++) {
                int p = orig.getPixel(i, j);
                int r = Color.red(p);
                int r_ref = Color.red(faceColor);
                if(Math.abs(r - r_ref) <= 5) {
                    mutableB.setPixel(i, j, refColor);
                }
            }
        }
        orig.recycle();
        return mutableB;
    }

    public void sendIntent(View view) {
        Intent intent = new Intent();
        intent.putExtra(Utils.QR_RESULT, "asdfasdf");

        setResult(Activity.RESULT_OK, intent);
        finish();
    }
}
