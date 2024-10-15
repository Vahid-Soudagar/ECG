/**
 * EcgGraphView is a custom view for displaying an ECG graph with square grid lines.
 * Author: Vahid Soudagar
 */

package com.vcreate.ecg;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class EcgGraphView extends SurfaceView implements SurfaceHolder.Callback {

    private Paint gridPaint;
    private Paint waveformPaint;
    private int mHeight;
    private int mWidth;

    private boolean isSurfaceViewAvailable;
    private int mDataBufferIndex;
    private List<Float> list;

    private float cellSize = 0;

    private String waveNumber;

    private int miniGridSide = 0;


    public EcgGraphView(Context context) {
        this(context, null);
        init();
    }

    public EcgGraphView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        init();
    }

    public EcgGraphView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        isSurfaceViewAvailable = true;
    }


    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        drawGridLines1(canvas);
        drawWaveForm1(canvas);
    }



    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        mWidth = width;
        mHeight = height;
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = (MeasureSpec.getSize(widthMeasureSpec));
        if (width > mWidth) mWidth = width;
        int height = (int) (MeasureSpec.getSize(heightMeasureSpec) * 0.95);
        if (height > mHeight) mHeight = height;
    }

    private void init() {
        mDataBufferIndex = 0;

        setBackgroundColor(ContextCompat.getColor(getContext(), R.color.ecg));
        gridPaint = new Paint();
        gridPaint.setColor(ContextCompat.getColor(getContext(), R.color.r1));
        gridPaint.setStrokeWidth(1);

        waveformPaint = new Paint();
        waveformPaint.setColor(ContextCompat.getColor(getContext(), R.color.black));
        waveformPaint.setStrokeWidth(2);
        waveformPaint.setAntiAlias(true);
    }

    private void drawGridLines1(Canvas canvas) {
        int canvasHeight = canvas.getHeight();
        int canvasWidth = canvas.getWidth();

        int miniGridHeight = canvasHeight / 8;
        int miniGridWidth = canvasWidth / 50;

        miniGridSide = 0;


        Log.d("EcgGraphView", "canvasHeight: " + canvasHeight + ", canvasWidth: " + canvasWidth);

        if (miniGridHeight < miniGridWidth) {
            // Recalculate based on miniGridHeight
            int recalculatedWidth = miniGridHeight * 50;
            if (recalculatedWidth <= canvasWidth) {
                // miniGridHeight works fine
                miniGridSide = miniGridHeight;
            } else {
                // Adjust to make sure it fits within width
                miniGridSide = canvasWidth / 50;
            }
        } else {
            int recalculatedHeight = miniGridWidth * 8;
            if (recalculatedHeight <= canvasHeight) {
                miniGridSide = miniGridWidth;
            } else {
                miniGridSide = canvasHeight / 8;
            }
        }

        Log.d("EcgGraphView", "miniGridSize "+miniGridSide  );

        // Paint for the fill color
        Paint fillPaint = new Paint();
        fillPaint.setColor(Color.parseColor("#FFCDD2")); // Light red color

        // Paint for the outline color
        Paint outlinePaint = new Paint();
        outlinePaint.setColor(Color.parseColor("#D32F2F")); // Dark red color
        outlinePaint.setStyle(Paint.Style.STROKE);
        outlinePaint.setStrokeWidth(2); // Set stroke width for the outline

        // Draw the grid
        for (int y = 0; y < canvasHeight; y += miniGridSide) {
            for (int x = 0; x < canvasWidth; x += miniGridSide) {
                canvas.drawRect(x, y, x + miniGridSide, y + miniGridSide, fillPaint);
                canvas.drawRect(x, y, x + miniGridSide, y + miniGridSide, outlinePaint);
            }
        }
    }





    private void drawWaveForm1(Canvas canvas) {
        if (list != null && !list.isEmpty()) {
            float maxValue = Collections.max(list);
            float minValue = Collections.min(list);
            float range = Math.max(maxValue - minValue, 1);
            float verticalScalingFactor = mHeight / (2 * range);

            float horizontalSpacing = (float) mWidth / list.size();

            float centerY = (float) mHeight / 2;

            float startX = 0;
            float startY = centerY - (list.get(0) - minValue) * verticalScalingFactor;

            for (int i = 1; i < list.size(); i++) {
                float endX = i * horizontalSpacing;
                float endY = centerY - (list.get(i) - minValue) * verticalScalingFactor;
                canvas.drawLine(startX, startY, endX, endY, waveformPaint);

                startX = endX;
                startY = endY;
            }
        }
    }




    public void addAmp(@NotNull List<Float> millivoltsList) {
        Log.d("TestData", "Adding data "+millivoltsList);
        this.list = millivoltsList;
    }


}
