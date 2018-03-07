package com.example.lucyzhao.votingapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by LucyZhao on 2018/3/6.
 */

public class LineIndicatorOverlay extends View {
    private float left, top, right, bottom;
    private float linePos = 0;
    private Paint paint;

    private static final int LINE_SPD = 5;
    private static final int FORWARD = 1;
    private static final int BACKWARD = -1;
    private int direction = FORWARD;

    private static final int DELAY = 10;

    public LineIndicatorOverlay(Context context) {
        super(context);
    }

    public LineIndicatorOverlay(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public LineIndicatorOverlay(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setStrokeWidth(2);

        left = 0;
        top = 0;
        right = w;
        bottom = h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawLine(left, linePos, right, linePos, paint);

        if(direction == FORWARD) {
            if(linePos < bottom) {
                linePos += LINE_SPD;
            }
            else {
                direction = BACKWARD;
            }
        }
        else {
            if(linePos > top) {
                linePos -= LINE_SPD;
            }
            else {
                direction = FORWARD;
            }
        }
        postInvalidateDelayed(DELAY);

    }
}
