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

import android.content.Context;
import android.widget.Toast;

import com.iwedia.dtv.DVBManager;
import com.iwedia.dtv.reminder.ReminderTimerParam;
import com.iwedia.dtv.types.InternalException;
import com.iwedia.dtv.types.TimerRepeatMode;
import com.iwedia.epg.R;

/**
 * Dialog for creating manual reminders.
 */
public class ManualReminderDialog extends ManualSetDialog {
    private Context mContext;

    public ManualReminderDialog(Context context, int width, int height) {
        super(context, width, height);
        mContext = context;
        mButtonEndTime.setEnabled(false);
    }

    @Override
    protected boolean createEventClicked() {
        if (mStartTime != null) {
            try {
                ReminderTimerParam param = new ReminderTimerParam(
                        mListViewChannels.getCheckedItemPosition()
                                + (DVBManager.getInstance()
                                        .isIpAndSomeOtherTunerType() ? 1 : 0),
                        TimerRepeatMode.ONCE, mStartTime);
                DVBManager.getInstance().createReminderManual(param);
                Toast.makeText(mContext, R.string.reminder_created,
                        Toast.LENGTH_SHORT).show();
                return true;
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (InternalException e) {
                e.printStackTrace();
                Toast.makeText(mContext, R.string.create_reminder_failed,
                        Toast.LENGTH_SHORT).show();
            }
        }
        return false;
    }
}
