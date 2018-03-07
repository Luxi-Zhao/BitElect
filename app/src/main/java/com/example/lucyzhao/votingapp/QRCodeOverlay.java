package com.example.lucyzhao.votingapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Region;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by LucyZhao on 2018/3/6.
 */

public class QRCodeOverlay extends View {
    private int mViewWidth;
    private int mViewHeight;
    private Path mRect;

    private float linePos = mViewHeight / 2;

    private Paint paint;

    public QRCodeOverlay(Context context) {
        super(context);

    }

    public QRCodeOverlay(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    public QRCodeOverlay(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

    }

    public QRCodeOverlay(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    /**
     * We cannot get the correct dimensions of views in onCreate because
     * they have not been inflated yet. This method is called every time the
     * size of a view changes, including the first time after it has been
     * inflated.
     *
     * @param w Current width of view.
     * @param h Current height of view.
     * @param oldw Previous width of view.
     * @param oldh Previous height of view.
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mViewWidth = w;
        mViewHeight = h;
        mRect = new Path();
        paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setStrokeWidth(2);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float margin = mViewWidth / 3;  //margin from middle to edge
        float left = (mViewWidth / 2) - margin;
        float top = (mViewHeight / 2) - margin;
        float right = (mViewWidth / 2) + margin;
        float bottom = (mViewHeight / 2) + margin;

        mRect.addRect(left, top, right, bottom, Path.Direction.CW);
        canvas.clipPath(mRect, Region.Op.DIFFERENCE);
        canvas.drawColor(Color.parseColor("#9600020D"));

        //if(linePos)

        canvas.drawLine(left, 500, right, 300, paint);
        mRect.rewind();
    }
}
