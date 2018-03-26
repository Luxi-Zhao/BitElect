package com.example.lucyzhao.votingapp;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.FaceDetector;
import android.util.Log;

/**
 * Created by LucyZhao on 2018/3/25.
 */

public class FaceComparer {
    private static final String TAG = FaceComparer.class.getSimpleName();
    private static final int SIM_THRESHOLD = 15;
    /**
     *
     * @param target passport photo
     * @param source picture taken by camera
     * @return similarity ratio
     */
    public static float compareImgs(Bitmap target, Bitmap source) {
        int smallH = Math.min(target.getHeight(), source.getHeight());
        int smallW = Math.min(target.getWidth(), source.getWidth());

        Bitmap scaledTarget = Bitmap.createScaledBitmap(target, smallW, smallH, false);
        Bitmap scaledSource = Bitmap.createScaledBitmap(source, smallW, smallH, false);

        Bitmap greyTarget = toGreyScale(scaledTarget);
        Bitmap greySource = toGreyScale(scaledSource);

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
        Log.v(TAG, "number of equals " + countEquals + " number of pixels " + numPixels);
        Log.v(TAG, "width * height " + greySource.getWidth() * greySource.getHeight());
        return percent * 100;
    }

    /**
     * Returns a greyscaled, cropped bitmap
     * Modifications: Recycles bitmap passed in
     * @param bitmap
     * @return greyscaled bitmap
     */
    private static Bitmap toGreyScale(Bitmap bitmap) {
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

    /**
     * Compares the similarity of two pixels
     * @param p1
     * @param p2
     * @return 1 if they are similar, 2 if either pixel is white, 0 of they are different
     */
    private static int pixelsAreEqual(int p1, int p2) {
        int r1 = Color.red(p1);
        int r2 = Color.red(p2);
        if(r1 == 0 || r2 == 0) return 2;
        else if(Math.abs(p1 - p2) <= SIM_THRESHOLD) return 1;
        else return 0;
    }
}
