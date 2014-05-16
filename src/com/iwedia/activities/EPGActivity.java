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
package com.iwedia.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.iwedia.adapters.FragmentTabAdapter;
import com.iwedia.adapters.ListViewChannelsAdapter;
import com.iwedia.dtv.DVBManager;
import com.iwedia.dtv.DVBManager.OnLoadFinishedListener;
import com.iwedia.dtv.epg.IEpgCallback;
import com.iwedia.dtv.types.InternalException;
import com.iwedia.epg.R;

import java.text.ParseException;

/**
 * EPGActivity - Show current EPG events of all channels for 24h.
 */
public class EPGActivity extends DVBActivity {
    private final String TAG = "ActivityEPG";
    public static final String FRAGMRENT_ARGUMENT_KEY_TIME = "time";
    public static final int HOURS = 24;
    /** Fragment Bundle Argument Keys */
    private FragmentTabAdapter mAdapterActivityEPGFragmentTab = null;
    private ListViewChannelsAdapter mAdapterActivityEPGListViewChannels = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.epg_activity);
        mAdapterActivityEPGFragmentTab = new FragmentTabAdapter(this);
        for (int i = 0; i < HOURS; i++) {
            Bundle lArguments = new Bundle();
            lArguments.putInt(FRAGMRENT_ARGUMENT_KEY_TIME, i);
            mAdapterActivityEPGFragmentTab.addTimeLine(lArguments);
        }
        mDVBManager.InitializeDTVService();
        mDVBManager.setEventCallback(mEventsCallback);
        mAdapterActivityEPGListViewChannels = new ListViewChannelsAdapter(this,
                mDVBManager.getChannelNames());
        try {
            mDVBManager.startDTV(0);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InternalException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        try {
            mDVBManager.removeEventCAllback(mEventsCallback);
            mDVBManager.stopDTV();
        } catch (InternalException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    public ListView getListViewChannels() {
        return mAdapterActivityEPGListViewChannels.getListViewChannels();
    }

    private IEpgCallback mEventsCallback = new IEpgCallback() {
        @Override
        public void scEventChanged(int arg0, int arg1) {
            Log.d(TAG, "EPG CALLBACK scEventChanged");
            loadEvents();
        }

        @Override
        public void scAcquisitionFinished(int arg0, int arg1) {
            Log.d(TAG, "EPG CALLBACK scAcquisitionFinished");
            loadEvents();
        }

        @Override
        public void pfEventChanged(int arg0, int arg1) {
            Log.d(TAG, "EPG CALLBACK pfEventChanged");
            loadEvents();
        }

        @Override
        public void pfAcquisitionFinished(int arg0, int arg1) {
            Log.d(TAG, "EPG CALLBACK pfAcquisitionFinished");
            loadEvents();
        }
    };

    /**
     * Load EPG events.
     */
    private void loadEvents() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mDVBManager.loadEvents(new OnLoadFinishedListener() {
                        @Override
                        public void onLoadFinished() {
                            notifyAdapters();
                        }
                    });
                } catch (ParseException e) {
                    Log.e(TAG, "Error in date parsing.", e);
                }
            }
        }).start();
    }

    public DVBManager getDVBManager() {
        return mDVBManager;
    }

    public void notifyAdapters() {
        mAdapterActivityEPGFragmentTab.notifyAdapters();
    }
}
