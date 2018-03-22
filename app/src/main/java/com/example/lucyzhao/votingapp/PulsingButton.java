package com.example.lucyzhao.votingapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by LucyZhao on 2018/3/14.
 */

public class PulsingButton extends android.support.v7.widget.AppCompatButton {
    private static final String TAG = PulsingButton.class.getSimpleName();
    private static final int DELAY = 10;
    private static final int SPD = 1;
    GradientDrawable btnBackground;

    private float radius;
    private float w, h;
    private List<Paint> btnPaints = new ArrayList<>();
    private int[] btnColors;
    private float[] marginX;
    private float[] marginY;

    private boolean animationOn = false;

    public PulsingButton(Context context) {
        super(context);
    }

    public PulsingButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PulsingButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // buttons are initially in unusable state
        setEnabled(false);
        stopAnimation();

        btnBackground = (GradientDrawable) getBackground().getCurrent();
        radius = btnBackground.getCornerRadius();

        btnColors = getResources().getIntArray(R.array.btnColors);
        marginX = new float[btnColors.length];
        marginY = new float[btnColors.length];

        for(int color : btnColors ) {
            Paint paint = new Paint();
            paint.setColor(color);
            paint.setStyle(Paint.Style.FILL);
            btnPaints.add(paint);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.w = w;
        this.h = h;

        float hw = w/2;
        float hh = h/2;

        int numColors = btnColors.length;
        for(int i = 1; i <= numColors; i++) {
            marginX[i - 1] = hw/numColors * i;
            marginY[i - 1] = hh/numColors * i;
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(animationOn) {
            for(int i = btnColors.length - 1; i >= 0; i--) {
                drawInnerRect(i, canvas);
            }
            postInvalidateDelayed(DELAY);
        }
    }

    private void drawInnerRect(int i, Canvas canvas) {
        float marginX = this.marginX[i];
        float marginY = this.marginY[i];
        if(marginX < w/2 && marginY < h/2) {
            canvas.drawRoundRect(w/2 - marginX, h/2 - marginY, w/2 + marginX, h/2 + marginY, radius, radius, btnPaints.get(i));
            this.marginX[i] += SPD;
            this.marginY[i] += h/w * SPD;
        }
        else {
            this.marginX[i] = 0;
            this.marginY[i] = 0;
        }
    }

    public void startAnimation() {
        animationOn = true;
    }

    public void stopAnimation() {
        animationOn = false;
    }

    public void enablePulsingButton() {
        startAnimation();
        setEnabled(true);
    }

    public void setCompleted() {
        stopAnimation();
        setEnabled(true);
        int color = ContextCompat.getColor(getContext(), R.color.colorBtnCompleted);
        btnBackground.setColor(color);
        btnBackground.setStroke(5, color);
        setTextColor(Color.WHITE);
    }

    public void setUncompleted() {
        setEnabled(false);
        int color = ContextCompat.getColor(getContext(), R.color.colorPrimary);
        btnBackground.setColor(color);
        btnBackground.setStroke(5, color);
    }
}
