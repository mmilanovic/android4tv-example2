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

import android.app.AlertDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TimePicker;

public class CustomTimePickerDialog extends AlertDialog implements
        DialogInterface.OnClickListener {
    private TimePicker mTimePicker;
    private int mInitialHourOfDay;
    private int mInitialMinute;
    private boolean mIs24HourView;
    private final OnTimeSetListener mCallback;
    private View.OnClickListener mHourUp = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int currentHour = mTimePicker.getCurrentHour();
            mTimePicker.setCurrentHour((currentHour + 1) % 24);
        }
    };
    private View.OnClickListener mHourDown = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int currentHour = mTimePicker.getCurrentHour();
            mTimePicker.setCurrentHour((--currentHour + 24) % 24);
        }
    };
    private View.OnClickListener mMinuteUp = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int currentMinute = mTimePicker.getCurrentMinute();
            mTimePicker.setCurrentMinute((currentMinute + 1) % 60);
        }
    };
    private View.OnClickListener mMinuteDown = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int currentMinute = mTimePicker.getCurrentMinute();
            mTimePicker.setCurrentMinute((--currentMinute + 60) % 60);
        }
    };

    public CustomTimePickerDialog(Context context, OnTimeSetListener callback,
            int hourOfDay, int minute, String title) {
        super(context);
        mCallback = callback;
        mInitialHourOfDay = hourOfDay;
        mInitialMinute = minute;
        mIs24HourView = true;
        /**
         * Create views
         */
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0,
                LayoutParams.WRAP_CONTENT, 1);
        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        LinearLayout up = new LinearLayout(context);
        up.setOrientation(LinearLayout.HORIZONTAL);
        container.addView(up);
        Button hourUp = new Button(context);
        hourUp.setOnClickListener(mHourUp);
        hourUp.setLayoutParams(params);
        hourUp.setText("UP");
        Button minuteUp = new Button(context);
        minuteUp.setOnClickListener(mMinuteUp);
        minuteUp.setLayoutParams(params);
        minuteUp.setText("UP");
        up.addView(hourUp);
        up.addView(minuteUp);
        mTimePicker = new TimePicker(context);
        container.addView(mTimePicker);
        LinearLayout down = new LinearLayout(context);
        down.setOrientation(LinearLayout.HORIZONTAL);
        container.addView(down);
        Button hourDown = new Button(context);
        hourDown.setOnClickListener(mHourDown);
        hourDown.setLayoutParams(params);
        hourDown.setText("DOWN");
        Button minuteDown = new Button(context);
        minuteDown.setOnClickListener(mMinuteDown);
        minuteDown.setLayoutParams(params);
        minuteDown.setText("DOWN");
        down.addView(hourDown);
        down.addView(minuteDown);
        // initialize state
        mTimePicker.setIs24HourView(mIs24HourView);
        mTimePicker.setCurrentHour(mInitialHourOfDay);
        mTimePicker.setCurrentMinute(mInitialMinute);
        setView(container);
        // set button
        setButton(BUTTON_POSITIVE, "Set", this);
        setTitle(title);
    }

    @Override
    public void show() {
        super.show();
        mTimePicker.setAddStatesFromChildren(true);
        mTimePicker
                .setDescendantFocusability(TimePicker.FOCUS_BLOCK_DESCENDANTS);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (mCallback != null) {
            mTimePicker.clearFocus();
            mCallback.onTimeSet(mTimePicker, mTimePicker.getCurrentHour(),
                    mTimePicker.getCurrentMinute());
        }
    }

    public TimePicker getmTimePicker() {
        return mTimePicker;
    }
}
