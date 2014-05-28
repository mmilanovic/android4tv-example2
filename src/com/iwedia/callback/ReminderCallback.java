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

import com.iwedia.dtv.reminder.IReminderCallback;
import com.iwedia.dtv.reminder.ReminderEventAdd;
import com.iwedia.dtv.reminder.ReminderEventRemove;
import com.iwedia.dtv.reminder.ReminderEventTrigger;
import com.iwedia.epg.R;

public class ReminderCallback implements IReminderCallback {
    private static final int MESSAGE_SHOW_TOAST = 0;
    private Context mContext = null;
    private Handler uiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_SHOW_TOAST:
                    Toast.makeText(mContext, R.string.reminder_created,
                            Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };
    private static ReminderCallback sInstance = null;

    public static ReminderCallback getInstance(Context mContext) {
        if (sInstance == null) {
            sInstance = new ReminderCallback(mContext);
        }
        return sInstance;
    }

    public static void destroyInstance() {
        sInstance = null;
    }

    private ReminderCallback(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    public void reminderAdd(ReminderEventAdd arg0) {
        uiHandler.sendEmptyMessage(MESSAGE_SHOW_TOAST);
    }

    @Override
    public void reminderRemove(ReminderEventRemove arg0) {
    }

    @Override
    public void reminderTrigger(ReminderEventTrigger arg0) {
    }
}
