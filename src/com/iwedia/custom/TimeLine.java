/*
 * Copyright (C) 2014 iWedia S.A.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.iwedia.custom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

/**
 * This view represents time line of 24h.
 */
public class TimeLine extends View {
    private final String TAG = "TimeLine";
    private final int STROKE_WIDTH = 4;
    private final int TEXT_SIZE = 13;
    private final int TEXT_HEIGHT = 10;
    private Paint mPaintLine = null;
    private Paint mPaintText = null;
    private Path mPathLine = null;
    private Rect mRectText = null;
    private String mTime = "9";

    public TimeLine(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeComponents();
    }

    private void initializeComponents() {
        mPaintLine = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintLine.setColor(Color.WHITE);
        mPaintLine.setStyle(Style.STROKE);
        mPaintLine.setStrokeWidth(STROKE_WIDTH);
        mPaintText = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintText.setColor(Color.WHITE);
        mPaintText.setTextSize(TEXT_SIZE);
        mRectText = new Rect();
    }

    /**
     * Create path of time line.
     * @param width
     *        View width.
     * @param height
     *        View height.
     * @return Path of time line.
     */
    private Path getPathLine(float width, float height) {
        if (mPathLine == null) {
            mPathLine = new Path();
            mPathLine.moveTo(0, 0);
            mPathLine.lineTo(0, height);
            mPathLine.close();
            mPathLine.moveTo(0, height / 2);
            mPathLine.lineTo(width, height / 2);
            mPathLine.close();
            mPathLine.moveTo(width / 2, height / 8);
            mPathLine.lineTo(width / 2, 7 * height / 8);
            mPathLine.close();
            mPathLine.moveTo(width / 6, height / 4);
            mPathLine.lineTo(width / 6, 3 * height / 4);
            mPathLine.close();
            mPathLine.moveTo(2 * width / 6, height / 4);
            mPathLine.lineTo(2 * width / 6, 3 * height / 4);
            mPathLine.close();
            mPathLine.moveTo(4 * width / 6, height / 4);
            mPathLine.lineTo(4 * width / 6, 3 * height / 4);
            mPathLine.close();
            mPathLine.moveTo(5 * width / 6, height / 4);
            mPathLine.lineTo(5 * width / 6, 3 * height / 4);
            mPathLine.close();
        }
        return mPathLine;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawPath(getPathLine(canvas.getWidth(), canvas.getHeight()),
                mPaintLine);
        mPaintText.getTextBounds(mTime + "h", 0, mTime.length() + 1, mRectText);
        canvas.drawText(mTime + "h", STROKE_WIDTH, TEXT_HEIGHT, mPaintText);
        canvas.drawText(mTime + "h", canvas.getWidth() / 2 - mRectText.width()
                - STROKE_WIDTH, TEXT_HEIGHT + STROKE_WIDTH / 2, mPaintText);
        canvas.drawText("30min", canvas.getWidth() / 2 + STROKE_WIDTH,
                TEXT_HEIGHT + STROKE_WIDTH / 2, mPaintText);
        super.onDraw(canvas);
    }

    public String getTime() {
        return mTime;
    }

    public void setTime(int mTime) {
        this.mTime = "" + mTime;
    }
}
