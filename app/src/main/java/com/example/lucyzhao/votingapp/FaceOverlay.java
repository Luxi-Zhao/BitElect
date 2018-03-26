package com.example.lucyzhao.votingapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.Landmark;

import java.util.List;

import static com.google.android.gms.vision.face.Landmark.LEFT_EYE;

/**
 * Created by LucyZhao on 2018/3/6.
 */

public class FaceOverlay extends View {
    private static final String TAG = FaceOverlay.class.getSimpleName();
    private static final int SCALE = 4;
    private Rect clip;
    private Rect boundary;
    private int left, top, right, bottom;
    private int w, h;

    private Paint boundaryPaint;
    private Paint decoPaint;

    private volatile Face face;


    public FaceOverlay(Context context) {
        super(context);

    }

    public FaceOverlay(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    public FaceOverlay(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

    }

    protected void setFace(@NonNull Face face) {
        this.face = face;
        postInvalidate();
    }



    /**
     * @param w    Current width of view.
     * @param h    Current height of view.
     * @param oldw Previous width of view.
     * @param oldh Previous height of view.
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        boundaryPaint = new Paint();
        decoPaint = new Paint();
        clip = new Rect();
        boundary = new Rect();

        decoPaint.setColor(Color.GREEN);
        decoPaint.setStrokeWidth(4);
        boundaryPaint.setColor(Color.RED);
        boundaryPaint.setStrokeWidth(3);
        boundaryPaint.setStyle(Paint.Style.STROKE);

        this.w = w;
        this.h = h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Face mFace = face;
        if(mFace == null) {
            Log.v(TAG, "mFace is null");
            return;
        }

        int left = (int) mFace.getPosition().x * SCALE;
        int top = (int) mFace.getPosition().y * SCALE;
        int right = left + (int) mFace.getWidth() * SCALE;
        int bot = top + (int) mFace.getHeight() * SCALE;


        boundary.set(w - left, top, w - right, bot);
        canvas.drawRect(boundary, boundaryPaint);

//        List<Landmark> landmarkList = mFace.getLandmarks();
//        Log.v(TAG, "lanmakrs size" + landmarkList.size());
//        for (Landmark landmark : landmarkList) {
//            int cx = (int) (landmark.getPosition().x * SCALE);
//            int cy = (int) (landmark.getPosition().y * SCALE);
//            canvas.drawCircle(w - cx, w - cy, 4, decoPaint);
//            Log.v(TAG, "cx cy" + cx + "  " + cy);
//        }

    }




}
