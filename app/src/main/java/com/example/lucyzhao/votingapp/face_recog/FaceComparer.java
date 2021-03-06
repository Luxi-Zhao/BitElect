package com.example.lucyzhao.votingapp.face_recog;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

/**
 * Created by LucyZhao on 2018/3/25.
 * <p>
 * Takes a passport photo and camera capture
 * Compares the two images pixel by pixel to return a similarity ratio
 * SimRatio is the percent of pixels that are considered "similar"
 * across the two images with respect to width*height
 */

public class FaceComparer {
    private static final String TAG = FaceComparer.class.getSimpleName();
    private static final int SIM_THRESHOLD = 25;      // pixel difference < 25 considered similar
    private static final int TOO_DARK_THRESHOLD = 10; // 10% of the pixels are too dark

    // static variables to communicate with the test class
    public static Bitmap greyTar, greySrc, maxSrc;

    /**
     * Top level function for the comparer
     * Compares passport photo with camera capture
     * Hairs are cropped to factor out fair style changes
     *
     * @param target passport photo
     * @param source camera capture
     * @return similarity ratio
     */
    public static float compareImgs(Bitmap target, Bitmap source) {
        Bitmap croppedTarget = cropHair(target);
        Bitmap croppedSource = cropHair(source);
        return compareImgsRec(croppedTarget, croppedSource, 0);
    }

    /**
     * Helper function for face comparison
     *
     * @param target passport photo
     * @param source picture taken by camera
     * @return similarity ratio
     */
    public static float compareImgsRec(Bitmap target, Bitmap source, int countRecs) {
        if (countRecs > 10) {
            return (float) 1.3;
        }

        // scale two images to the same size
        int smallH = Math.min(target.getHeight(), source.getHeight());
        int smallW = Math.min(target.getWidth(), source.getWidth());

        Bitmap scaledTarget = Bitmap.createScaledBitmap(target, smallW, smallH, false);
        Bitmap scaledSource = Bitmap.createScaledBitmap(source, smallW, smallH, false);

        // gray scale them
        Bitmap greyTarget = toGreyScale(scaledTarget);
        Bitmap greySource = toGreyScale(scaledSource);
        greySrc = greySource;
        greyTar = greyTarget;

        int countEquals = 0;    // number of pixels that are similar
        int countTooDark = 0;   // number of pixels that are too dark
        int numPixels = greySource.getWidth() * greySource.getHeight();
        Log.v(TAG, "original number of pixels " + numPixels);
        for (int i = 0; i < greyTarget.getWidth(); i++) {
            for (int j = 0; j < greyTarget.getHeight(); j++) {
                int p1 = greySource.getPixel(i, j);
                int p2 = greyTarget.getPixel(i, j);
                int ret = pixelsAreEqual(p1, p2);
                if (ret == 1) {
                    countEquals++;
                } else if (ret == 2) {
                    numPixels--;
                }
                if (pixelTooDark(p1)) countTooDark++;
            }
        }

        float percent = (float) countEquals / (float) numPixels;
        Log.v(TAG, "number of equals " + countEquals + " number of pixels without white pixels" + numPixels);

        float compareResult = percent * 100;
        float tooDarkRatio = (float) countTooDark / (float) numPixels * 100;
        Log.v(TAG, "too dark ratio" + tooDarkRatio);

        //if 10% of the pixels are "too dark", we brighten the picture and compare again
        if (tooDarkRatio > TOO_DARK_THRESHOLD) {
            Log.v(TAG, "picture too dark");
            greySource = brightenImg(greySource, tooDarkRatio);
            Log.v(TAG, "enter recursion");
            compareResult = compareImgsRec(greyTarget, greySource, countRecs + 1);
        } else {
            Log.v(TAG, "picture not too dark");
        }
        return compareResult;
    }

    ///////////////////////////////////////////////
    ///////////IMAGE TRANSFORMATIONS///////////////
    ///////////////////////////////////////////////

    /**
     * Returns a greyscaled bitmap
     * Modifications: Recycles bitmap passed in
     *
     * @param bitmap
     * @return greyscaled bitmap
     */
    private static Bitmap toGreyScale(Bitmap bitmap) {
        int size = bitmap.getByteCount();
        int[] pixels = new int[size];
        int[] greyPs = new int[size];
        bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        for (int i = 0; i < size; i++) {
            int p = pixels[i];
            int r = Color.red(p);
            int g = Color.green(p);
            int b = Color.blue(p);
            int avg = (r + g + b) / 3;
            greyPs[i] = Color.rgb(avg, avg, avg);
        }
        // each pixel stored as 4 byte
        Bitmap ret = Bitmap.createBitmap(greyPs, bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);

        bitmap.recycle();
        return ret;
    }

    /**
     * Reduces the image's height to crop out the forehead area
     *
     * @param bitmap original bitmap
     * @return bitmap with hair cropped
     */
    private static Bitmap cropHair(Bitmap bitmap) {
        return Bitmap.createBitmap(bitmap, 0, (int) (bitmap.getHeight() * (2.0 / 6.0)), bitmap.getWidth(), (int) (bitmap.getHeight() * (4.0 / 6.0)));
    }

    /**
     * Increases the overall pixel value of the bitmap, making it brighter
     *
     * @param bitmap        original image
     * @param darknessRatio a measure of how dark the original picture is
     *                      10 means 10% pixels are too dark
     * @return brightened image
     */
    private static Bitmap brightenImg(Bitmap bitmap, float darknessRatio) {

        int size = bitmap.getByteCount();
        int[] pixels = new int[size];
        int[] brightPs = new int[size];
        bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        for (int i = 0; i < size; i++) {
            int p1 = pixels[i];
            int r = Color.red(p1) + (int) (255 * (darknessRatio / 100));
            int p2 = Color.rgb(r, r, r);
            brightPs[i] = p2;
        }
        Bitmap ret = Bitmap.createBitmap(brightPs, bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        return ret;
    }

    ///////////////////////////////////////////////
    //////////////CHECKER METHODS//////////////////
    ///////////////////////////////////////////////

    /**
     * Checks the similarity and whiteness of the pixels
     *
     * @return 1 if they are similar
     * 2 if either pixel is white
     * 0 of they are different
     */
    private static int pixelsAreEqual(int p1, int p2) {
        int r1 = Color.red(p1);
        int r2 = Color.red(p2);
        if (r1 > 250 || r2 > 250) return 2;
        else if (Math.abs(p1 - p2) <= SIM_THRESHOLD) return 1;
        else return 0;
    }

    /**
     * Checks whether the pixel is considered too dark
     */
    private static boolean pixelTooDark(int p) {
        int r = Color.red(p);
        return r < 20;
    }

}
