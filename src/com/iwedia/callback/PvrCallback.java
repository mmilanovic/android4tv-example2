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
package com.iwedia.callback;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.iwedia.dtv.pvr.IPvrCallback;
import com.iwedia.dtv.pvr.PvrEventMediaAdd;
import com.iwedia.dtv.pvr.PvrEventMediaRemove;
import com.iwedia.dtv.pvr.PvrEventPlaybackJump;
import com.iwedia.dtv.pvr.PvrEventPlaybackPosition;
import com.iwedia.dtv.pvr.PvrEventPlaybackSpeed;
import com.iwedia.dtv.pvr.PvrEventPlaybackStart;
import com.iwedia.dtv.pvr.PvrEventPlaybackStop;
import com.iwedia.dtv.pvr.PvrEventRecordAdd;
import com.iwedia.dtv.pvr.PvrEventRecordConflict;
import com.iwedia.dtv.pvr.PvrEventRecordPosition;
import com.iwedia.dtv.pvr.PvrEventRecordRemove;
import com.iwedia.dtv.pvr.PvrEventRecordResourceIssue;
import com.iwedia.dtv.pvr.PvrEventRecordStart;
import com.iwedia.dtv.pvr.PvrEventRecordStop;
import com.iwedia.dtv.pvr.PvrEventTimeshiftJump;
import com.iwedia.dtv.pvr.PvrEventTimeshiftPosition;
import com.iwedia.dtv.pvr.PvrEventTimeshiftSpeed;
import com.iwedia.dtv.pvr.PvrEventTimeshiftStart;
import com.iwedia.dtv.pvr.PvrEventTimeshiftStop;
import com.iwedia.epg.R;

public class PvrCallback implements IPvrCallback {
    private static final int MESSAGE_SHOW_TOAST_SUCCESS = 0,
            MESSAGE_SHOW_TOAST_CONFLICT = 1,
            MESSAGE_SHOW_TOAST_RECORD_STARTED = 2,
            MESSAGE_SHOW_TOAST_RECORD_STOPPED = 3;
    private Context mContext = null;
    private Handler uiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_SHOW_TOAST_SUCCESS: {
                    Toast.makeText(mContext, R.string.smart_record_notification,
                            Toast.LENGTH_SHORT).show();
                    break;
                }
                case MESSAGE_SHOW_TOAST_CONFLICT: {
                    Toast.makeText(mContext, R.string.smart_record_failed,
                            Toast.LENGTH_SHORT).show();
                    break;
                }
                case MESSAGE_SHOW_TOAST_RECORD_STARTED: {
                    Toast.makeText(mContext, R.string.record_started,
                            Toast.LENGTH_SHORT).show();
                    break;
                }
                case MESSAGE_SHOW_TOAST_RECORD_STOPPED: {
                    Toast.makeText(mContext, R.string.record_stopped,
                            Toast.LENGTH_SHORT).show();
                    break;
                }
                default:
                    break;
            }
        }
    };
    private static PvrCallback sInstance = null;

    public static PvrCallback getInstance(Context mContext) {
        if (sInstance == null) {
            sInstance = new PvrCallback(mContext);
        }
        return sInstance;
    }

    private PvrCallback(Context mContext) {
        this.mContext = mContext;
    }

    public static void destroyInstance() {
        sInstance = null;
    }

    @Override
    public void eventDeviceError() {
    }

    @Override
    public void eventMediaAdd(PvrEventMediaAdd arg0) {
    }

    @Override
    public void eventMediaRemove(PvrEventMediaRemove arg0) {
    }

    @Override
    public void eventPlaybackJump(PvrEventPlaybackJump arg0) {
    }

    @Override
    public void eventPlaybackPosition(PvrEventPlaybackPosition arg0) {
    }

    @Override
    public void eventPlaybackSpeed(PvrEventPlaybackSpeed arg0) {
    }

    @Override
    public void eventPlaybackStart(PvrEventPlaybackStart arg0) {
    }

    @Override
    public void eventPlaybackStop(PvrEventPlaybackStop arg0) {
    }

    @Override
    public void eventRecordAdd(PvrEventRecordAdd arg0) {
        uiHandler.sendEmptyMessage(MESSAGE_SHOW_TOAST_SUCCESS);
    }

    @Override
    public void eventRecordConflict(PvrEventRecordConflict arg0) {
        uiHandler.sendEmptyMessage(MESSAGE_SHOW_TOAST_CONFLICT);
    }

    @Override
    public void eventRecordPosition(PvrEventRecordPosition arg0) {
    }

    @Override
    public void eventRecordRemove(PvrEventRecordRemove arg0) {
    }

    @Override
    public void eventRecordResourceIssue(PvrEventRecordResourceIssue arg0) {
    }

    @Override
    public void eventRecordStart(PvrEventRecordStart arg0) {
        uiHandler.sendEmptyMessage(MESSAGE_SHOW_TOAST_RECORD_STARTED);

    }

    @Override
    public void eventRecordStop(PvrEventRecordStop arg0) {
        uiHandler.sendEmptyMessage(MESSAGE_SHOW_TOAST_RECORD_STOPPED);

    }

    @Override
    public void eventTimeshiftJump(PvrEventTimeshiftJump arg0) {
    }

    @Override
    public void eventTimeshiftPosition(PvrEventTimeshiftPosition arg0) {
    }

    @Override
    public void eventTimeshiftSpeed(PvrEventTimeshiftSpeed arg0) {
    }

    @Override
    public void eventTimeshiftStart(PvrEventTimeshiftStart arg0) {
    }

    @Override
    public void eventTimeshiftStop(PvrEventTimeshiftStop arg0) {
    }
}
