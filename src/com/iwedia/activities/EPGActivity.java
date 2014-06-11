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

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;

import com.iwedia.adapters.FragmentTabAdapter;
import com.iwedia.adapters.ListViewChannelsAdapter;
import com.iwedia.dtv.DVBManager;
import com.iwedia.dtv.DVBManager.OnLoadFinishedListener;
import com.iwedia.dtv.epg.EpgEventGenre;
import com.iwedia.epg.R;

import java.text.ParseException;

/**
 * EPGActivity - Show current EPG events of all channels for 24h.
 */
public class EPGActivity extends DTVActivity implements OnMenuItemClickListener {
    private final String TAG = "ActivityEPG";
    public static final String FRAGMRENT_ARGUMENT_KEY_TIME = "time";
    public static final int HOURS = 24;
    /** Fragment Bundle Argument Keys */
    private FragmentTabAdapter mAdapterActivityEPGFragmentTab = null;
    private ListViewChannelsAdapter mAdapterActivityEPGListViewChannels = null;
    private ProgressDialog mProgressDialog;
    private PopupMenu mPopup;
    private OnLoadFinishedListener mOnLoadFinishedListener = new OnLoadFinishedListener() {
        @Override
        public void onLoadFinished(String date) {
            mAdapterActivityEPGFragmentTab.notifyAdapters(date);
        }
    };

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
        /** Initialzie progress dialog */
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle(R.string.progress_info);
        /** Load EPG events initially */
        mDVBManager.reLoadEvents();
    }

    /** Listener for menu button click */
    public void onClickMenu(View v) {
        // openOptionsMenu();
        if (v == null) {
            v = findViewById(R.id.textview_date);
        }
        // create popup menu
        if (mPopup == null) {
            mPopup = new PopupMenu(this, v);
            mPopup.setOnMenuItemClickListener(this);
            MenuInflater inflater = mPopup.getMenuInflater();
            inflater.inflate(R.menu.epg_genre, mPopup.getMenu());
        }
        /**
         * Set active genre state.
         */
        /** ALL */
        MenuItem checkable = mPopup.getMenu().findItem(R.id.menu_genre_all);
        checkable
                .setChecked(mDVBManager.getActiveGenre() == EpgEventGenre.GENRE_ALL ? true
                        : false);
        /** CHILDREN */
        checkable = mPopup.getMenu().findItem(R.id.menu_genre_children);
        checkable
                .setChecked(mDVBManager.getActiveGenre() == EpgEventGenre.CHILDREN_YOUTH_PROGRAMMES ? true
                        : false);
        /** CULTURE */
        checkable = mPopup.getMenu().findItem(R.id.menu_genre_culture);
        checkable
                .setChecked(mDVBManager.getActiveGenre() == EpgEventGenre.ARTS_CULTURE ? true
                        : false);
        /** HOBBIES */
        checkable = mPopup.getMenu().findItem(R.id.menu_genre_hobbies);
        checkable
                .setChecked(mDVBManager.getActiveGenre() == EpgEventGenre.LEISURE_HOBBIES ? true
                        : false);
        /** MOVIES */
        checkable = mPopup.getMenu().findItem(R.id.menu_genre_movie);
        checkable
                .setChecked(mDVBManager.getActiveGenre() == EpgEventGenre.MOVIE_DRAMA ? true
                        : false);
        /** MUSIC */
        checkable = mPopup.getMenu().findItem(R.id.menu_genre_music);
        checkable
                .setChecked(mDVBManager.getActiveGenre() == EpgEventGenre.MUSIC_BALLET_DANCE ? true
                        : false);
        /** NEWS */
        checkable = mPopup.getMenu().findItem(R.id.menu_genre_news);
        checkable
                .setChecked(mDVBManager.getActiveGenre() == EpgEventGenre.NEWS_CURRENT_AFFAIRS ? true
                        : false);
        /** POLITIC */
        checkable = mPopup.getMenu().findItem(R.id.menu_genre_politic);
        checkable
                .setChecked(mDVBManager.getActiveGenre() == EpgEventGenre.SOCIAL_POLITICAL_ISSUES_ECONOMICS ? true
                        : false);
        /** SCIENCE */
        checkable = mPopup.getMenu().findItem(R.id.menu_genre_science);
        checkable
                .setChecked(mDVBManager.getActiveGenre() == EpgEventGenre.EDUCATION_SCIENCE_FACTUAL_TOPICS ? true
                        : false);
        /** SHOW */
        checkable = mPopup.getMenu().findItem(R.id.menu_genre_show);
        checkable
                .setChecked(mDVBManager.getActiveGenre() == EpgEventGenre.SHOW_GAME_SHOW ? true
                        : false);
        /** SPORTS */
        checkable = mPopup.getMenu().findItem(R.id.menu_genre_sports);
        checkable
                .setChecked(mDVBManager.getActiveGenre() == EpgEventGenre.SPORTS ? true
                        : false);
        mPopup.show();
    }

    @Override
    public boolean onMenuItemClick(final MenuItem item) {
        /** Handle item selection. */
        if (!item.isChecked()) {
            item.setChecked(true);
        }
        switch (item.getItemId()) {
            case R.id.menu_genre_all: {
                mDVBManager.setGenreFilter(EpgEventGenre.GENRE_ALL);
                break;
            }
            case R.id.menu_genre_children: {
                mDVBManager
                        .setGenreFilter(EpgEventGenre.CHILDREN_YOUTH_PROGRAMMES);
                break;
            }
            case R.id.menu_genre_culture: {
                mDVBManager.setGenreFilter(EpgEventGenre.ARTS_CULTURE);
                break;
            }
            case R.id.menu_genre_hobbies: {
                mDVBManager.setGenreFilter(EpgEventGenre.LEISURE_HOBBIES);
                break;
            }
            case R.id.menu_genre_movie: {
                mDVBManager.setGenreFilter(EpgEventGenre.MOVIE_DRAMA);
                break;
            }
            case R.id.menu_genre_music: {
                mDVBManager.setGenreFilter(EpgEventGenre.MUSIC_BALLET_DANCE);
                break;
            }
            case R.id.menu_genre_news: {
                mDVBManager.setGenreFilter(EpgEventGenre.NEWS_CURRENT_AFFAIRS);
                break;
            }
            case R.id.menu_genre_politic: {
                mDVBManager
                        .setGenreFilter(EpgEventGenre.SOCIAL_POLITICAL_ISSUES_ECONOMICS);
                break;
            }
            case R.id.menu_genre_science: {
                mDVBManager
                        .setGenreFilter(EpgEventGenre.EDUCATION_SCIENCE_FACTUAL_TOPICS);
                break;
            }
            case R.id.menu_genre_show: {
                mDVBManager.setGenreFilter(EpgEventGenre.SHOW_GAME_SHOW);
                break;
            }
            case R.id.menu_genre_sports: {
                mDVBManager.setGenreFilter(EpgEventGenre.SPORTS);
                break;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
        mProgressDialog.show();
        mDVBManager.reLoadEvents();
        return true;
    }

    /**
     * Get Genre by index.
     */
    public static String getEPGGenre(int genre) {
        int lStringId = -1;
        switch (genre) {
            case 0x1:
                lStringId = R.string.genre_movie_drama;
                break;
            case 0x2:
                lStringId = R.string.genre_news_current_affairs;
                break;
            case 0x3:
                lStringId = R.string.genre_show_game_show;
                break;
            case 0x4:
                lStringId = R.string.genre_sports;
                break;
            case 0x5:
                lStringId = R.string.genre_children_youth_programmes;
                break;
            case 0x6:
                lStringId = R.string.genre_music_ballet_dance;
                break;
            case 0x7:
                lStringId = R.string.genre_arts_culture;
                break;
            case 0x8:
                lStringId = R.string.genre_social_political_issues;
                break;
            case 0x9:
                lStringId = R.string.genre_education_science;
                break;
            case 0xA:
                lStringId = R.string.genre_leisure_hobbies;
                break;
        }
        return lStringId == -1 ? "" : sInstance.getApplicationContext()
                .getString(lStringId);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            onClickMenu(null);
            return true;
        }
        return super.onKeyUp(keyCode, event);
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

    public ProgressDialog getProgressDialog() {
        return mProgressDialog;
    }

    public ListView getListViewChannels() {
        return mAdapterActivityEPGListViewChannels.getListViewChannels();
    }

    public DVBManager getDVBManager() {
        return mDVBManager;
    }
}
