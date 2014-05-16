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
package com.iwedia.fragments;

import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ListView;

import com.iwedia.activities.EPGActivity;
import com.iwedia.adapters.ListViewTimeAdapter;
import com.iwedia.custom.TimeLine;
import com.iwedia.custom.TimeLineObject;
import com.iwedia.dtv.DVBManager.OnLoadFinishedListener;
import com.iwedia.dtv.types.InternalException;
import com.iwedia.epg.R;

import java.text.ParseException;

/**
 * This fragment inflates listview who will show events for one hour.
 */
public class EPGFragment extends Fragment implements OnItemSelectedListener,
        OnItemClickListener, OnItemLongClickListener {
    private final String TAG = "FragmentEPG";
    private View mView = null;
    private ListView mListView = null;
    private NotifyFragments mNotifyFragments = null;
    private int mPosition = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        if (mView == null) {
            mView = inflater.inflate(R.layout.epg_time_fragment, container,
                    false);
            mListView = ((ListView) mView.findViewById(R.id.listview_epg));
            mListView.setAdapter(new ListViewTimeAdapter(getActivity(),
                    ((EPGActivity) getActivity()).getDVBManager()
                            .getChannelNames(), ((EPGActivity) getActivity())
                            .getDVBManager().getLoadedEpgEvents(),
                    getArguments().getInt(
                            EPGActivity.FRAGMRENT_ARGUMENT_KEY_TIME)));
            mListView.setOnItemSelectedListener(this);
            mListView.setOnItemClickListener(this);
            mListView.setOnItemLongClickListener(this);
            mListView.setSelection(mPosition);
            ((TimeLine) mView.findViewById(R.id.timeline_epg_time))
                    .setTime(getArguments().getInt(
                            EPGActivity.FRAGMRENT_ARGUMENT_KEY_TIME));
        } else {
            ((ViewGroup) mView.getParent()).removeView(mView);
        }
        return mView;
    }

    /**
     * Set position of event and channel list view, positions have to be same.
     * @param position
     *        Position of item.
     */
    public void setListViewPosition(int position) {
        if (mListView != null) {
            mPosition = position;
            mListView.setSelection(position);
            ((EPGActivity) getActivity()).getListViewChannels().setSelection(
                    position);
        }
    }

    /**
     * Get Position of event in listview.
     * @return Position of item.
     */
    public int getListViewPosition() {
        if (mListView != null) {
            return mListView.getSelectedItemPosition();
        }
        return 0;
    }

    /**
     * Create new adapter with new data.
     * @throws RemoteException
     */
    public void reInitializeAdapter() throws RemoteException {
        /** If Fragment is not shown (initialized) do nothing */
        if (mView != null) {
            mListView.setAdapter(new ListViewTimeAdapter(getActivity(),
                    ((EPGActivity) getActivity()).getDVBManager()
                            .getChannelNames(), ((EPGActivity) getActivity())
                            .getDVBManager().getLoadedEpgEvents(),
                    getArguments().getInt(
                            EPGActivity.FRAGMRENT_ARGUMENT_KEY_TIME)));
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position,
            long id) {
        if (mNotifyFragments != null) {
            mNotifyFragments.listViewChanged();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
        try {
            ((EPGActivity) getActivity()).getDVBManager()
                    .changeChannelByNumber(position);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InternalException e) {
            e.printStackTrace();
        }
        ((TimeLineObject) view).showDialogWithEvents();
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
    }

    /**
     * Set callback for fragments
     * @param notifyFragments
     *        Object of NotifyFragments
     */
    public void setNotifyFragments(NotifyFragments notifyFragments) {
        mNotifyFragments = notifyFragments;
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view,
            int position, long id) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ((EPGActivity) getActivity()).getDVBManager().loadEvents(
                            new OnLoadFinishedListener() {
                                @Override
                                public void onLoadFinished() {
                                    ((EPGActivity) getActivity())
                                            .notifyAdapters();
                                }
                            });
                } catch (ParseException e) {
                    Log.e(TAG, "Error in date parsing.", e);
                }
            }
        }).start();
        return true;
    }
}
