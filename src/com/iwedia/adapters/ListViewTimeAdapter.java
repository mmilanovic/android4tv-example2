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
package com.iwedia.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.iwedia.custom.TimeLineObject;
import com.iwedia.dtv.TimeEvent;
import com.iwedia.dtv.TimeEventHolder;
import com.iwedia.epg.R;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Adapter for listview who contains events (whose duration is one hour) for all
 * channels.
 */
public class ListViewTimeAdapter extends BaseAdapter {
    private final String TAG = "AdapterActivityEPGListView";
    private LayoutInflater mLayoutInflater = null;
    private ArrayList<String> mChannelNames = null;
    private ArrayList<ArrayList<TimeEventHolder>> mTimeEventHolders = null;
    private Context mContext = null;

    public ListViewTimeAdapter(Activity activity,
            ArrayList<String> channelNames, ArrayList<TimeEvent> timeEvents,
            int time) {
        mContext = activity.getApplicationContext();
        mLayoutInflater = activity.getLayoutInflater();
        mChannelNames = channelNames;
        initializeEvents(timeEvents, time);
    }

    public ListViewTimeAdapter(FragmentActivity activity,
            ArrayList<String> channelNames, TimeEvent[] timeEvents, int time) {
        mContext = activity.getApplicationContext();
        mLayoutInflater = activity.getLayoutInflater();
        mChannelNames = channelNames;
        ArrayList<TimeEvent> events = new ArrayList<TimeEvent>(
                Arrays.asList(timeEvents));
        initializeEvents(events, time);
    }

    private void initializeEvents(ArrayList<TimeEvent> timeEvents, int time) {
        try {
            if (null != timeEvents) {
                TimeEvent lTimeEvent = timeEvents.get(time);
                if (null != lTimeEvent) {
                    mTimeEventHolders = lTimeEvent.getEvents();
                    if (mTimeEventHolders == null) {
                        mTimeEventHolders = new ArrayList<ArrayList<TimeEventHolder>>();
                    }
                }
            }
        } catch (IndexOutOfBoundsException e) {
            Log.w(TAG, "There are no Time Events.", e);
        }
    }

    @Override
    public int getCount() {
        return mChannelNames.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.epg_time_item, null);
            convertView.setLayoutParams(new ListView.LayoutParams(
                    ListView.LayoutParams.MATCH_PARENT, (int) mContext
                            .getResources().getDimensionPixelSize(
                                    R.dimen.epg_item_height)));
        }
        setView((TimeLineObject) convertView, position);
        return convertView;
    }

    private void setView(TimeLineObject holder, int position) {
        if (null != mTimeEventHolders && mTimeEventHolders.size() > 0) {
            holder.setEvents(mTimeEventHolders.get(position),
                    mChannelNames.get(position));
        } else {
            holder.setEvents(null, mChannelNames.get(position));
        }
    }
}
