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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.widget.TextView;

import com.iwedia.activities.EPGActivity;
import com.iwedia.dtv.DVBManager;
import com.iwedia.epg.R;
import com.iwedia.fragments.EPGFragment;
import com.iwedia.fragments.EPGFragment.NotifyFragments;

import java.text.ParseException;
import java.util.ArrayList;

/**
 * Adapter with fragments for viewpager, there are 24 fragments which one
 * represents one hour.
 */
public class FragmentTabAdapter extends FragmentPagerAdapter implements
        ViewPager.OnPageChangeListener {
    private final String TAG = "FragmentTabAdapter";
    private EPGActivity mActivity = null;
    private ViewPager mViewPager = null;
    private final ArrayList<EPGFragment> mFragments = new ArrayList<EPGFragment>();
    private int mPosition = 0;
    private Handler mHandler = null;
    /** Alert Dialog for Previous/Next Day EPG. */
    private AlertDialog mEPGDayAlertDialog = null;
    /** TextView for Date. */
    private TextView mTextViewDate = null;

    public FragmentTabAdapter(EPGActivity activity) {
        super(activity.getSupportFragmentManager());
        mActivity = activity;
        mViewPager = (ViewPager) activity.findViewById(R.id.viewpager_epg);
        mViewPager.setAdapter(this);
        mViewPager.setOnPageChangeListener(this);
        mViewPager.setPageMargin(-5);
        mTextViewDate = (TextView) activity.findViewById(R.id.textview_date);
        mHandler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                mTextViewDate.setText((String) msg.obj);
                try {
                    notifyAllAdapters();
                } catch (RemoteException e) {
                    Log.e(TAG, "Error with service connection.", e);
                }
            };
        };
        initializeEPGAlertDialog();
    }

    private void initializeEPGAlertDialog() {
        AlertDialog.Builder lEPGDayAlertBuilder = new AlertDialog.Builder(
                mActivity);
        lEPGDayAlertBuilder.setPositiveButton(R.string.yes,
                new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    if (mPosition == 23) {
                                        mActivity.getDVBManager().loadEvents(
                                                DVBManager.LOAD_EPG_NEXT_DAY);
                                    } else if (mPosition == 0) {
                                        mActivity
                                                .getDVBManager()
                                                .loadEvents(
                                                        DVBManager.LOAD_EPG_PREVIOUS_DAY);
                                    }
                                } catch (ParseException e) {
                                    Log.e(TAG, "Error in date parsing.", e);
                                }
                            }
                        }).start();
                    }
                });
        lEPGDayAlertBuilder.setNegativeButton(R.string.no,
                new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mEPGDayAlertDialog.cancel();
                    }
                });
        mEPGDayAlertDialog = lEPGDayAlertBuilder.create();
    }

    /**
     * Add fragment to viewpager.
     * @param time
     *        Time who fragment will represent.
     */
    public void addTimeLine(Bundle time) {
        EPGFragment lFragment = null;
        lFragment = new EPGFragment();
        lFragment.setArguments(time);
        lFragment.setNotifyFragments(new NotifyFragments() {
            @Override
            public void listViewChanged() {
                notifyAllFragments();
            }

            @Override
            public boolean showAlertDialog() {
                if (mPosition == 23) {
                    mEPGDayAlertDialog.setMessage(mActivity
                            .getString(R.string.show_epg_next));
                    mEPGDayAlertDialog.show();
                    return true;
                } else if (mPosition == 0) {
                    mEPGDayAlertDialog.setMessage(mActivity
                            .getString(R.string.show_epg_previous));
                    mEPGDayAlertDialog.show();
                    return true;
                }
                return false;
            }
        });
        mFragments.add(lFragment);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }

    @Override
    public float getPageWidth(int position) {
        return .25f;
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments.get(position);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset,
            int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        notifyAllFragments();
        mPosition = position;
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    /**
     * When position of list view is changed all other fragments should be
     * notified to update their position of listviews too.
     */
    private void notifyAllFragments() {
        for (int i = 0; i < mFragments.size(); i++) {
            mFragments.get(i).setListViewPosition(
                    mFragments.get(mPosition).getListViewPosition());
        }
    }

    /**
     * If there are new events, update old ones.
     * @throws RemoteException
     */
    private void notifyAllAdapters() throws RemoteException {
        for (int i = 0; i < mFragments.size(); i++) {
            mFragments.get(i).reInitializeAdapter();
        }
    }

    /**
     * When callback arrives for new events, update view.
     */
    public void notifyAdapters(String date) {
        Message.obtain(mHandler, 0, date).sendToTarget();
    }
}
