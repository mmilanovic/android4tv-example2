package com.iwedia.callback;

import android.util.Log;

import com.iwedia.dtv.DVBManager;
import com.iwedia.dtv.epg.IEpgCallback;

import java.text.ParseException;

public class EPGCallBack implements IEpgCallback {
    private static final String TAG = "EPGCallBack";
    private DVBManager mDVBManager = null;

    public EPGCallBack(DVBManager dvbManager) {
        mDVBManager = dvbManager;
    }

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

    /**
     * Reload EPG events.
     */
    private void loadEvents() {
        try {
            mDVBManager.loadEvents(DVBManager.LOAD_EPG_CURRENT_DAY);
        } catch (ParseException e) {
            Log.e(TAG, "There was an error in reloading EPG events.", e);
        }
    }
}
