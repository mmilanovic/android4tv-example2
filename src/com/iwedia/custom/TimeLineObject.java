/*
 * Copyright (C) 2014 iWedia S.A. Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package com.iwedia.custom;

import android.app.Dialog;
import android.app.Service;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.iwedia.dtv.DVBManager;
import com.iwedia.dtv.TimeEventHolder;
import com.iwedia.dtv.pvr.SmartCreateParams;
import com.iwedia.dtv.reminder.ReminderSmartParam;
import com.iwedia.dtv.types.InternalException;
import com.iwedia.epg.R;

import java.util.ArrayList;

/**
 * This custom view represents visually duration and time of the event.
 */
public class TimeLineObject extends View {
    private final String TAG = "TimeLineObject";
    private Paint mPaintTime = null;
    private Paint mPaintPath = null;
    private ArrayList<TimeEventHolder> mTimeEventHolder = null;
    private Context mContext = null;
    private String mChannelName = "";

    public TimeLineObject(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initializeComponents();
    }

    private void initializeComponents() {
        mPaintTime = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintTime.setColor(Color.MAGENTA);
        mPaintTime.setStyle(Style.FILL);
        mPaintPath = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintPath.setColor(Color.BLACK);
        mPaintPath.setStyle(Style.STROKE);
        mPaintPath.setStrokeWidth(5);
    }

    /**
     * Draw rectangle with specific width according to begin and end time.
     * 
     * @param canvas
     *        Canvas from view.
     */
    private void drawTimeLine(Canvas canvas) {
        if (null != mTimeEventHolder) {
            for (TimeEventHolder timeHolder : mTimeEventHolder) {
                int lHours = 0;
                if (timeHolder.getEndTime().getHours() > timeHolder
                        .getBeginTime().getHours()) {
                    lHours = timeHolder.getEndTime().getHours()
                            - timeHolder.getBeginTime().getHours();
                }
                int lBeginMinutes = timeHolder.getBeginTime().getMinutes() == 0 ? 1
                        : timeHolder.getBeginTime().getMinutes();
                int lEndMinutes = (lHours * 60)
                        + timeHolder.getEndTime().getMinutes() == 0 ? 1
                        : timeHolder.getEndTime().getMinutes();
                float lXBeginPosition = lBeginMinutes * canvas.getWidth() / 59;
                float lXEndPosition = lEndMinutes * canvas.getWidth() / 59;
                canvas.drawRect(lXBeginPosition, 0, lXEndPosition,
                        canvas.getHeight(), mPaintTime);
                drawBorders(canvas, lXEndPosition);
            }
        }
    }

    /**
     * Draw black borders at the end of every event, because events are the same
     * color and this helps user to recognize one event.
     * 
     * @param canvas
     *        Canvas from view.
     * @param xEndPosition
     *        End position of rectangle.
     */
    private void drawBorders(Canvas canvas, float xEndPosition) {
        Path lPath = new Path();
        if (xEndPosition < canvas.getWidth()) {
            lPath.moveTo(xEndPosition, 0);
            lPath.lineTo(xEndPosition, canvas.getHeight());
            lPath.close();
            canvas.drawPath(lPath, mPaintPath);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawTimeLine(canvas);
        super.onDraw(canvas);
    }

    public void setEvents(ArrayList<TimeEventHolder> events, String channelName) {
        mTimeEventHolder = events;
        mChannelName = channelName;
    }

    /**
     * For detailed information about events in duration of one hour.
     */
    public void showDialogWithEvents() {
        if (null != mTimeEventHolder && mTimeEventHolder.size() > 0) {
            View lView = ((LayoutInflater) mContext
                    .getSystemService(Service.LAYOUT_INFLATER_SERVICE))
                    .inflate(R.layout.epg_events_dialog, null);
            LinearLayout lLinearLayout = (LinearLayout) lView
                    .findViewById(R.id.linearlayout_events);
            Button lPvrRecord = (Button) lView
                    .findViewById(R.id.buttonCreateSmartRecord);
            Button lReminder = (Button) lView
                    .findViewById(R.id.buttonCreateReminder);
            final TimeEventHolder holder = mTimeEventHolder.get(0);
            TextView lTextView = new TextView(mContext);
            lTextView.setText(holder.toString());
            lLinearLayout.addView(lTextView);
            lPvrRecord.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        DVBManager.getInstance().createSmartRecord(
                                new SmartCreateParams(holder.getEvent()
                                        .getServiceIndex(), holder.getEvent()
                                        .getEventId(), holder.getEvent()
                                        .getName(), holder.getEvent()
                                        .getDescription(), holder.getEvent()
                                        .getStartTime(), holder.getEvent()
                                        .getEndTime()));
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (InternalException e) {
                        e.printStackTrace();
                    }
                }
            });
            lReminder.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        DVBManager.getInstance().createReminder(
                                new ReminderSmartParam(holder.getEvent()
                                        .getName(), holder.getEvent()
                                        .getDescription(), holder.getEvent()
                                        .getServiceIndex(), 0, holder
                                        .getEvent().getEventId(), holder
                                        .getEvent().getStartTime()));
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (InternalException e) {
                        e.printStackTrace();
                    }
                }
            });
            Dialog lDialog = new Dialog(mContext);
            lDialog.setTitle(mChannelName);
            lDialog.setContentView(lView);
            lDialog.show();
        }
    }
}
