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

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.iwedia.epg.R;
import com.iwedia.fragments.EPGFragment;
import com.iwedia.fragments.NotifyFragments;

import java.util.ArrayList;

/**
 * Adapter with fragments for viewpager, there are 24 fragments which one
 * represents one hour.
 */
public class FragmentTabAdapter extends FragmentPagerAdapter implements
        ViewPager.OnPageChangeListener {
    private final String TAG = "FragmentTabAdapter";
    private Context mContext = null;
    private ViewPager mViewPager = null;
    private final ArrayList<EPGFragment> mFragments = new ArrayList<EPGFragment>();
    private int mPosition = 0;
    private Handler mHandler = null;

    public FragmentTabAdapter(final FragmentActivity activity) {
        super(activity.getSupportFragmentManager());
        mContext = activity;
        mViewPager = (ViewPager) activity.findViewById(R.id.viewpager_epg);
        mViewPager.setAdapter(this);
        mViewPager.setOnPageChangeListener(this);
        mViewPager.setPageMargin(-5);
        mHandler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                try {
                    notifyAllAdapters();
                } catch (RemoteException e) {
                    Log.e(TAG, "Error with service connection.", e);
                }
            };
        };
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
    public void notifyAdapters() {
        mHandler.sendEmptyMessage(0);
    }
}
