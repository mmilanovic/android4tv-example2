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
package com.iwedia.adapters;

import android.app.Activity;
import android.app.Service;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.iwedia.epg.R;

import java.util.ArrayList;

/**
 * Adapter for listview with channel names.
 */
public class ListViewChannelsAdapter extends BaseAdapter {
    private final String TAG = "AdapterActivityEPGListView";
    private ListView mListViewChannels = null;
    private LayoutInflater mLayoutInflater = null;
    private ArrayList<String> mChannelNames = null;

    public ListViewChannelsAdapter(Activity activity,
            ArrayList<String> channelNames) {
        mChannelNames = channelNames;
        mListViewChannels = (ListView) activity.findViewById(R.id.listview_epg);
        mLayoutInflater = (LayoutInflater) activity
                .getSystemService(Service.LAYOUT_INFLATER_SERVICE);
        mListViewChannels = (ListView) activity.findViewById(R.id.listview_epg);
        mListViewChannels.setAdapter(this);
        mListViewChannels.setFocusable(false);
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
        ViewHolder lViewHolder = null;
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.epg_channel_item,
                    null);
            lViewHolder = new ViewHolder(convertView);
            convertView.setTag(lViewHolder);
        } else {
            lViewHolder = (ViewHolder) convertView.getTag();
        }
        setView(lViewHolder, position);
        return convertView;
    }

    public ListView getListViewChannels() {
        return mListViewChannels;
    }

    private void setView(ViewHolder holder, int position) {
        holder.getTextView().setText(mChannelNames.get(position));
    }

    private class ViewHolder {
        private TextView mTextView = null;

        public ViewHolder(View convertView) {
            mTextView = (TextView) convertView
                    .findViewById(R.id.textview_channel_number);
        }

        public TextView getTextView() {
            return mTextView;
        }
    }
}
