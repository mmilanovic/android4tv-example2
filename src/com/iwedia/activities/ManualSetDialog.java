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
package com.iwedia.activities;

import android.app.Dialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.iwedia.dtv.DVBManager;
import com.iwedia.dtv.types.InternalException;
import com.iwedia.dtv.types.TimeDate;
import com.iwedia.epg.R;

import java.util.ArrayList;

/**
 * Manual set dialog.
 */
public abstract class ManualSetDialog extends Dialog implements
        android.view.View.OnClickListener {
    private static final int MESSAGE_REFRESH_TIME = 0;
    protected ListView mListViewChannels;
    protected TextView mTimeFromStream;
    protected Button mButtonStartTime, mButtonEndTime, mButtonCreate;
    private Context mContext;
    protected TimeDate mStartTime, mEndTime;
    private Thread mTimerThread;
    /** CallBack Handler */
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_REFRESH_TIME: {
                    try {
                        TimeDate timeFromStream = DVBManager.getInstance()
                                .getCurrentTime();
                        StringBuilder builder = new StringBuilder();
                        mTimeFromStream.setText(builder
                                .append(timeFromStream.getHour()).append(":")
                                .append(timeFromStream.getMin()).toString());
                    } catch (InternalException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                default:
                    break;
            }
        }
    };

    public ManualSetDialog(Context context, int width, int height) {
        super(context, R.style.DialogTransparent);
        mContext = context;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // Hide Status Bar of Android.
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        getWindow().setFormat(PixelFormat.RGBA_8888);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DITHER);
        setContentView(R.layout.timer_set_dialog);
        getWindow().getAttributes().width = width;
        getWindow().getAttributes().height = height;
        init();
        initializeChannelList();
    }

    /**
     * Initialize views.
     */
    private void init() {
        mTimeFromStream = (TextView) findViewById(R.id.textViewTimeFromStream);
        mListViewChannels = (ListView) findViewById(R.id.listViewChannels);
        mButtonStartTime = (Button) findViewById(R.id.buttonStartTime);
        mButtonEndTime = (Button) findViewById(R.id.buttonEndTime);
        mButtonCreate = (Button) findViewById(R.id.buttonCreate);
        mButtonStartTime.setOnClickListener(this);
        mButtonEndTime.setOnClickListener(this);
        mButtonCreate.setOnClickListener(this);
    }

    private void initializeChannelList() {
        ArrayList<String> channelNames = new ArrayList<String>();
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(mContext,
                android.R.layout.simple_list_item_single_choice);
        try {
            channelNames = DVBManager.getInstance().getChannelNames();
        } catch (InternalException e) {
            e.printStackTrace();
        }
        int size = channelNames.size();
        for (int i = 0; i < size; i++) {
            arrayAdapter.add(channelNames.get(i));
        }
        mListViewChannels.setAdapter(arrayAdapter);
    }

    @Override
    public void show() {
        try {
            int activeChannel = DVBManager.getInstance()
                    .getCurrentChannelNumber();
            mListViewChannels.setSelection(activeChannel);
            mListViewChannels.setItemChecked(activeChannel, true);
        } catch (InternalException e) {
            e.printStackTrace();
        }
        mButtonStartTime.setText("--:--");
        mButtonEndTime.setText("--:--");
        startThread();
        super.show();
    }

    @Override
    public void onBackPressed() {
        mStartTime = null;
        mEndTime = null;
        stopThread();
        super.onBackPressed();
    }

    protected abstract boolean createEventClicked();

    @Override
    public void onClick(View v) {
        try {
            final TimeDate timeFromStream = DVBManager.getInstance()
                    .getCurrentTime();
            if (timeFromStream != null) {
                switch (v.getId()) {
                    case R.id.buttonEndTime: {
                        CustomTimePickerDialog timePicker = new CustomTimePickerDialog(
                                mContext, new OnTimeSetListener() {
                                    @Override
                                    public void onTimeSet(TimePicker view,
                                            int hourOfDay, int minute) {
                                        mEndTime = new TimeDate(timeFromStream
                                                .getSec(), timeFromStream
                                                .getMin(), timeFromStream
                                                .getHour(), timeFromStream
                                                .getDay(), timeFromStream
                                                .getMonth(), timeFromStream
                                                .getYear());
                                        mEndTime.setHour(hourOfDay);
                                        mEndTime.setMin(minute);
                                        mButtonEndTime.setText(String.format(
                                                "%02d", hourOfDay)
                                                + ":"
                                                + String.format("%02d", minute));
                                    }
                                }, timeFromStream.getHour(), timeFromStream
                                        .getMin(), "Set end time");
                        timePicker.show();
                        break;
                    }
                    case R.id.buttonStartTime: {
                        CustomTimePickerDialog timePicker = new CustomTimePickerDialog(
                                mContext, new OnTimeSetListener() {
                                    @Override
                                    public void onTimeSet(TimePicker view,
                                            int hourOfDay, int minute) {
                                        mStartTime = new TimeDate(
                                                timeFromStream.getSec(),
                                                timeFromStream.getMin(),
                                                timeFromStream.getHour(),
                                                timeFromStream.getDay(),
                                                timeFromStream.getMonth(),
                                                timeFromStream.getYear());
                                        mStartTime.setHour(hourOfDay);
                                        mStartTime.setMin(minute);
                                        mButtonStartTime.setText(String.format(
                                                "%02d", hourOfDay)
                                                + ":"
                                                + String.format("%02d", minute));
                                    }
                                }, timeFromStream.getHour(), timeFromStream
                                        .getMin(), "Set start time");
                        timePicker.show();
                        break;
                    }
                    case R.id.buttonCreate: {
                        if (createEventClicked()) {
                            onBackPressed();
                        }
                        break;
                    }
                    default:
                        break;
                }
            }
        } catch (InternalException e) {
            e.printStackTrace();
        }
    }

    /**
     * Starts background thread.
     */
    private synchronized void startThread() {
        stopThread();
        mTimerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Thread thisThread = Thread.currentThread();
                while (true) {
                    if (thisThread == mTimerThread) {
                        mHandler.sendEmptyMessage(MESSAGE_REFRESH_TIME);
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }
                }
            }
        });
        mTimerThread.setPriority(Thread.MIN_PRIORITY);
        mTimerThread.start();
    }

    /**
     * Stops background thread.
     */
    private synchronized void stopThread() {
        if (mTimerThread != null) {
            Thread moribund = mTimerThread;
            mTimerThread = null;
            moribund.interrupt();
        }
    }
}
