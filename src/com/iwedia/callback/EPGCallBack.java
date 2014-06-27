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
        mDVBManager.reLoadEvents();
    }

    @Override
    public void scAcquisitionFinished(int arg0, int arg1) {
        Log.d(TAG, "EPG CALLBACK scAcquisitionFinished");
        mDVBManager.reLoadEvents();
    }

    @Override
    public void pfEventChanged(int arg0, int arg1) {
        Log.d(TAG, "EPG CALLBACK pfEventChanged");
    }

    @Override
    public void pfAcquisitionFinished(int arg0, int arg1) {
        Log.d(TAG, "EPG CALLBACK pfAcquisitionFinished");
    }
}
