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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.iwedia.activities.TVActivity;
import com.iwedia.dtv.ChannelInfo;
import com.iwedia.dtv.DVBManager;
import com.iwedia.dtv.reminder.IReminderCallback;
import com.iwedia.dtv.reminder.ReminderEventAdd;
import com.iwedia.dtv.reminder.ReminderEventRemove;
import com.iwedia.dtv.reminder.ReminderEventTrigger;
import com.iwedia.dtv.types.InternalException;
import com.iwedia.epg.R;

public class ReminderCallback implements IReminderCallback {
    private static final int MESSAGE_SHOW_TOAST_ADDED = 0,
            MESSAGE_SHOW_TOAST_REMOVED = 1, REMINDER_TRIGERED = 2;
    private TVActivity mContext = null;
    private AlertDialog mReminderDialog;
    private DVBManager mDvbManager;
    private Handler uiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_SHOW_TOAST_ADDED: {
                    Toast.makeText(mContext, R.string.reminder_created,
                            Toast.LENGTH_SHORT).show();
                    break;
                }
                case MESSAGE_SHOW_TOAST_REMOVED: {
                    Toast.makeText(mContext, R.string.reminder_deleted,
                            Toast.LENGTH_SHORT).show();
                    if (mContext.getmReminderListDialog() != null
                            && mContext.getmReminderListDialog().isShowing()) {
                        mContext.getmReminderListDialog().updateRecords();
                    }
                    break;
                }
                case REMINDER_TRIGERED: {
                    if (mContext.getmReminderListDialog() != null
                            && mContext.getmReminderListDialog().isShowing()) {
                        mContext.getmReminderListDialog().updateRecords();
                    }
                    Dialog dialog = (Dialog) msg.obj;
                    dialog.show();
                    break;
                }
                default:
                    break;
            }
        }
    };
    private static ReminderCallback sInstance = null;

    public static ReminderCallback getInstance(TVActivity mContext) {
        if (sInstance == null) {
            sInstance = new ReminderCallback(mContext);
        }
        return sInstance;
    }

    public static void destroyInstance() {
        sInstance = null;
    }

    private ReminderCallback(TVActivity mContext) {
        this.mContext = mContext;
        try {
            mDvbManager = DVBManager.getInstance();
        } catch (InternalException e) {
            e.printStackTrace();
        }
        /**
         * Initialize alert dialog.
         */
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(mContext);
        builderSingle.setTitle("Reminder triggered");
        builderSingle.setNegativeButton("Close",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        mReminderDialog = builderSingle.create();
    }

    @Override
    public void reminderAdd(ReminderEventAdd arg0) {
        uiHandler.sendEmptyMessage(MESSAGE_SHOW_TOAST_ADDED);
    }

    @Override
    public void reminderRemove(ReminderEventRemove arg0) {
        uiHandler.sendEmptyMessage(MESSAGE_SHOW_TOAST_REMOVED);
    }

    @Override
    public void reminderTrigger(ReminderEventTrigger arg0) {
        Log.d("ReminderCallback",
                "\n\n\nREMINDER TRIGGERED: " + arg0.getTitle());
        ChannelInfo info = mDvbManager.getChannelInfo(arg0.getServiceIndex()
                - (mDvbManager.isIpAndSomeOtherTunerType() ? 1 : 0));
        mReminderDialog.setMessage(arg0.getTitle() + " - [" + info.getName()
                + "]");
        Message.obtain(uiHandler, REMINDER_TRIGERED, mReminderDialog)
                .sendToTarget();
    }
}
