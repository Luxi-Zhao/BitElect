package com.example.lucyzhao.votingapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Region;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by LucyZhao on 2018/3/6.
 */

public class QRCodeOverlay extends View {
    private int mViewWidth;
    private int mViewHeight;

    private Rect clip;
    private Rect boundary;
    private int left, top, right, bottom;
    private int margin = -1;

    private Paint boundaryPaint;
    private Paint decoPaint;


    public QRCodeOverlay(Context context) {
        super(context);

    }

    public QRCodeOverlay(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    public QRCodeOverlay(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

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
        boundaryPaint = new Paint();
        decoPaint = new Paint();
        clip = new Rect();
        boundary = new Rect();
        boundaryPaint.setColor(Color.WHITE);
        boundaryPaint.setStrokeWidth(2);
        decoPaint.setColor(Color.GREEN);
        decoPaint.setStrokeWidth(4);

        margin = mViewWidth / 3;  //margin from middle to edge
        left = (mViewWidth / 2) - margin;
        top = (mViewHeight / 2) - margin;
        right = (mViewWidth / 2) + margin;
        bottom = (mViewHeight / 2) + margin;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        clip.set(left, top, right, bottom);
        int bLeft = left - 3, bTop = top - 3, bRight = right + 3, bBottom = bottom + 3;
        boundary.set(bLeft, bTop, bRight, bBottom);
        canvas.clipRect(clip, Region.Op.DIFFERENCE);

        int linelen = margin / 5;
        canvas.drawColor(Color.parseColor("#801e1404"));
        canvas.drawRect(boundary, boundaryPaint);
        canvas.drawLine(bLeft, bTop, bLeft + linelen, bTop, decoPaint);
        canvas.drawLine(bRight - linelen, bTop, bRight, bTop, decoPaint);
        canvas.drawLine(bLeft, bBottom, bLeft + linelen, bBottom, decoPaint);
        canvas.drawLine(bRight - linelen, bBottom, bRight, bBottom, decoPaint);

        canvas.drawLine(bLeft, bTop, bLeft, bTop + linelen, decoPaint);
        canvas.drawLine(bRight, bTop, bRight, bTop + linelen, decoPaint);
        canvas.drawLine(bLeft, bBottom - linelen, bLeft, bBottom, decoPaint);
        canvas.drawLine(bRight, bBottom - linelen, bRight, bBottom, decoPaint);
    }

    public int getMargin() {
        return margin;
    }

}
