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
import android.widget.ListView;

import com.iwedia.adapters.FragmentTabAdapter;
import com.iwedia.adapters.ListViewChannelsAdapter;
import com.iwedia.dtv.DVBManager;
import com.iwedia.dtv.DVBManager.OnLoadFinishedListener;
import com.iwedia.dtv.types.TimeDate;
import com.iwedia.epg.R;

/**
 * EPGActivity - Show current EPG events of all channels for 24h.
 */
public class EPGActivity extends DTVActivity {
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
        mDVBManager.setLoadFinishedListener(mOnLoadFinishedListener);
        mAdapterActivityEPGFragmentTab = new FragmentTabAdapter(this);
        for (int i = 0; i < HOURS; i++) {
            Bundle lArguments = new Bundle();
            lArguments.putInt(FRAGMRENT_ARGUMENT_KEY_TIME, i);
            mAdapterActivityEPGFragmentTab.addTimeLine(lArguments);
        }
        mAdapterActivityEPGListViewChannels = new ListViewChannelsAdapter(this,
                mDVBManager.getChannelNames());
        /** Initialize EPG Date. */
        mDVBManager.initializeDate();
    }

    public ListView getListViewChannels() {
        return mAdapterActivityEPGListViewChannels.getListViewChannels();
    }

    private OnLoadFinishedListener mOnLoadFinishedListener = new OnLoadFinishedListener() {
        @Override
        public void onLoadFinished(String date) {
            mAdapterActivityEPGFragmentTab.notifyAdapters(date);
        }
    };

    public DVBManager getDVBManager() {
        return mDVBManager;
    }

    /**
     * Get Genre by index.
     */
    public static String getEPGGenre(int genre) {
        int lStringId = -1;
        switch (genre) {
            case 0:
                lStringId = com.iwedia.epg.R.string.epg_genre_all;
                break;
            case 1:
                lStringId = com.iwedia.epg.R.string.epg_genre_movies;
                break;
            case 2:
                lStringId = com.iwedia.epg.R.string.epg_genre_news;
                break;
            case 3:
                lStringId = com.iwedia.epg.R.string.epg_genre_show;
                break;
            case 4:
                lStringId = com.iwedia.epg.R.string.epg_genre_politics;
                break;
            case 6:
                lStringId = com.iwedia.epg.R.string.epg_genre_children;
                break;
            case 7:
                lStringId = com.iwedia.epg.R.string.epg_genre_culture;
                break;
            case 8:
                lStringId = com.iwedia.epg.R.string.epg_genre_politics;
                break;
            case 9:
                lStringId = com.iwedia.epg.R.string.epg_genre_science;
                break;
            case 10:
                lStringId = com.iwedia.epg.R.string.epg_genre_hobies;
                break;
            default:
                return "";
        }
        return sInstance.getApplicationContext().getString(lStringId);
    }

    /**
     * Get Parental Rating by Index.
     */
    public static String getParentalRating(int rate) {
        if (rate >= 4 && rate <= 18) {
            return sInstance.getApplicationContext().getString(
                    R.string.parental_under)
                    + rate;
        }
        return sInstance.getApplicationContext()
                .getString(R.string.parental_no);
    }
}
