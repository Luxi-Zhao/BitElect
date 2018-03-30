package com.example.lucyzhao.votingapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by LucyZhao on 2018/3/29.
 */

public class BarGraph extends View {
    private Paint cand1Paint;
    private Paint cand2Paint;
    private Paint linePaint;
    private Paint textPaint;
    private Rect bar;

    private double w, h;
    private int result1 = 10, result2 = 20;
    private String cand1FN = "Luxi", cand1LN = "Zhao", cand2FN="Kanglong", cand2LN="Qiu";
    private static final double GRAPH_MARGIN_RATIO = 1.0 / 8.0;
    private double graphMargin;
    private double marginVert, lineY;
    private double maxBarHeight;
    private double[] barHeights = new double[2];
    private double barWidth;

    private static final int LINE_STROKE_WIDTH = 5;

    private int[] barAnimHeights = {0, 0};
    private static final int BAR_GRAPH_SPD = 5;
    private static final int REFRESH_DELAY = 10;

    private static final int TEXT_MARGIN = 20;


    public BarGraph(Context context) {
        super(context);
    }

    public BarGraph(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public BarGraph(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setPollResult(int result1, int result2) {
        this.result1 = result1;
        this.result2 = result2;
    }

    public void setCandNames(String cand1FN, String cand1LN, String cand2FN, String cand2LN) {
        this.cand1FN = cand1FN;
        this.cand1LN = cand1LN;
        this.cand2FN = cand2FN;
        this.cand2LN = cand2LN;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.w = (double) w;
        this.h = (double) h;

        cand1Paint = new Paint();
        cand2Paint = new Paint();
        linePaint = new Paint();
        textPaint = new Paint();

        cand1Paint.setColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
        cand2Paint.setColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryDark));
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(LINE_STROKE_WIDTH);
        linePaint.setColor(ContextCompat.getColor(getContext(), R.color.colorGrey));
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setColor(ContextCompat.getColor(getContext(), R.color.colorGrey));
        textPaint.setAntiAlias(true);
        textPaint.setTextSize((float) (this.w / 15.0));
        textPaint.setTextAlign(Paint.Align.CENTER);

        bar = new Rect();

        graphMargin = this.w * GRAPH_MARGIN_RATIO;
        marginVert = this.h * 1.0 / 6.0;
        lineY = h - marginVert + LINE_STROKE_WIDTH;
        maxBarHeight = this.h - 2 * marginVert;
        barWidth = this.w * 1.0 / 6.0;
        barHeights = getRelativeHeight(maxBarHeight, result1, result2);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawHorizontalLine(canvas);

        int[] nums = calcNumbers();
        String num1 = Integer.toString(nums[0]);
        String num2 = Integer.toString(nums[1]);

        drawBar(barAnimHeights[0], canvas, true);
        drawBar(barAnimHeights[1], canvas, false);
        drawNumber(num1, canvas, true);
        drawNumber(num2, canvas, false);
        drawLabels(canvas, cand1FN, cand1LN, cand2FN, cand2LN);
        boolean refresh = false;
        for (int i = 0; i < barAnimHeights.length; i++) {
            if (barAnimHeights[i] < barHeights[i]) {
                barAnimHeights[i] += BAR_GRAPH_SPD;
                refresh = true;
            }
        }

        if (refresh) postInvalidateDelayed(REFRESH_DELAY);
    }

    /**
     * @param height bar height in pixels
     * @param canvas
     * @param candID true for candidate 1, false for candidate 2
     */
    private void drawBar(double height, Canvas canvas, boolean candID) {
        double left, top, right, bottom;
        top = h - marginVert - height;
        bottom = h - marginVert;

        Paint p;
        if (!candID) {
            left = w / 2.0 + graphMargin;
            right = left + barWidth;
            p = cand2Paint;
        } else {
            right = w / 2.0 - graphMargin;
            left = right - barWidth;
            p = cand1Paint;
        }

        bar.set((int) left, (int) top, (int) right, (int) bottom);
        canvas.drawRect(bar, p);
    }

    /**
     *
     * @param num
     * @param canvas
     * @param candID true for cand 1, false for cand 2
     */
    private void drawNumber(String num, Canvas canvas, boolean candID) {
        double barCenter = getBarCenter(candID);
        double x = barCenter;
        int cand = candID ? 0 : 1;
        double y = lineY - barAnimHeights[cand] - TEXT_MARGIN;
        canvas.drawText(num, (float) x, (float) y, textPaint);
    }

    private int[] calcNumbers() {
        int[] nums = new int[2];
        nums[0] = (int) ((result1 + result2) * (barAnimHeights[0] / maxBarHeight));
        nums[1] = (int) ((result1 + result2) * (barAnimHeights[1] / maxBarHeight));
        return nums;
    }

    private static double[] getRelativeHeight(double maxGraphHeight, double result1, double result2) {

        double weight1 = result1 / (result1 + result2);
        double weight2 = result2 / (result1 + result2);
        double[] ret = new double[2];
        ret[0] = maxGraphHeight * weight1;
        ret[1] = maxGraphHeight * weight2;
        return ret;
    }

    private void drawHorizontalLine(Canvas canvas) {
        double startX, stopX;
        startX = graphMargin;
        stopX = w - graphMargin;

        canvas.drawLine((int) startX, (int) lineY, (int) stopX, (int) lineY, linePaint);
    }

    private void drawLabels(Canvas canvas, String cand1FN, String cand1LN, String cand2FN, String cand2LN) {
        double x1, x2, y;
        x1 = getBarCenter(true);
        x2 = getBarCenter(false);

        Rect bounds = new Rect();
        textPaint.getTextBounds("f", 0, 1, bounds);
        int boxH = bounds.height();
        y = lineY + boxH + TEXT_MARGIN ;

        canvas.drawText(cand1LN, (float)x1, (float)y, textPaint);
        canvas.drawText(cand2LN, (float)x2, (float)y, textPaint);
        canvas.drawText(cand1FN, (float)x1, (float)y+boxH+TEXT_MARGIN, textPaint);
        canvas.drawText(cand2FN, (float)x2, (float)y+boxH+TEXT_MARGIN, textPaint);
    }

    private double getBarCenter(boolean candID) {
        if(candID) {
            return w / 2.0 - graphMargin - barWidth / 2.0;
        }
        else return w / 2.0 + graphMargin + barWidth / 2.0;
    }
}
