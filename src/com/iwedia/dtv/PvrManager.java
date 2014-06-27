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
package com.iwedia.dtv;

import com.iwedia.dtv.dtvmanager.IDTVManager;
import com.iwedia.dtv.pvr.IPvrCallback;
import com.iwedia.dtv.pvr.IPvrControl;
import com.iwedia.dtv.pvr.PvrRecordType;
import com.iwedia.dtv.pvr.PvrSortMode;
import com.iwedia.dtv.pvr.PvrSortOrder;

import java.util.ArrayList;

/**
 * Class for PVR related functions.
 */
public class PvrManager {
    private IPvrControl mPvrControl;
    private IPvrCallback mPvrCallback;
    private static PvrManager instance = null;

    protected static PvrManager getInstance(IDTVManager mDTVManager) {
        if (instance == null) {
            instance = new PvrManager(mDTVManager);
        }
        return instance;
    }

    private PvrManager(IDTVManager mDTVManager) {
        mPvrControl = mDTVManager.getPvrControl();
    }

    /**
     * Registers PVR callback.
     * 
     * @param callback
     *        Callback to register.
     */
    public void registerPvrCallback(IPvrCallback callback) {
        mPvrCallback = callback;
        mPvrControl.registerCallback(callback);
    }

    /**
     * Unregisters PVR callback.
     */
    public void unregisterPvrCallback() {
        if (mPvrCallback != null) {
            mPvrControl.unregisterCallback(mPvrCallback);
        }
    }

    /**
     * Sets PVR media path.
     * 
     * @param mediaPath
     *        Path to set.
     */
    public void setMediaPath(String mediaPath) {
        mPvrControl.setDevicePath(mediaPath);
    }

    /**
     * Retrieves list of scheduled records.
     * 
     * @return List of scheduled records.
     */
    public ArrayList<Object> getPvrScheduledRecords() {
        ArrayList<Object> records = new ArrayList<Object>();
        int numberOfMediaRecords = mPvrControl.updateRecordList();
        for (int i = 0; i < numberOfMediaRecords; i++) {
            PvrRecordType type = mPvrControl.getRecordType(i);
            if (type == PvrRecordType.ONTOUCH) {
                records.add(mPvrControl.getOnTouchInfo(i));
            } else if (type == PvrRecordType.SMART) {
                records.add(mPvrControl.getSmartInfo(i));
            } else {
                records.add(mPvrControl.getTimerInfo(i));
            }
        }
        return records;
    }

    /**
     * Delete scheduled PVR record.
     * 
     * @param index
     *        of scheduled record to delete.
     */
    public void deleteScheduledRecord(int index) {
        mPvrControl.destroyRecord(index);
    }

    /**
     * Sets desired sort mode.
     * 
     * @param order
     *        New sort mode to set.
     */
    public void setScheduledSortMode(PvrSortMode mode) {
        mPvrControl.setRecordListSortMode(mode);
    }

    /**
     * Returns active sort mode.
     */
    public PvrSortMode getScheduledSortMode() {
        return mPvrControl.getRecordListSortMode();
    }

    /**
     * Sets desired sort order.
     * 
     * @param order
     *        New sort order to set.
     */
    public void setScheduledSortOrder(PvrSortOrder order) {
        mPvrControl.setRecordListSortOrder(order);
    }

    /**
     * Returns active sort order.
     */
    public PvrSortOrder getScheduledSortOrder() {
        return mPvrControl.getRecordListSortOrder();
    }
}
