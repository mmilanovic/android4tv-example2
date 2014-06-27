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
import com.iwedia.dtv.pvr.TimerCreateParams;
import com.iwedia.dtv.types.InternalException;
import com.iwedia.dtv.types.TimerRepeatMode;
import com.iwedia.epg.R;

/**
 * Dialog for creating manual PVR records.
 */
public class ManualPvrRecordDialog extends ManualSetDialog {
    private Context mContext;

    public ManualPvrRecordDialog(Context context, int width, int height) {
        super(context, width, height);
        mContext = context;
    }

    @Override
    protected boolean createEventClicked() {
        if (mStartTime != null && mEndTime != null
                && mStartTime.getCalendar().before(mEndTime.getCalendar())) {
            try {
                TimerCreateParams params = new TimerCreateParams(
                        mListViewChannels.getCheckedItemPosition()
                                + (DVBManager.getInstance()
                                        .isIpAndSomeOtherTunerType() ? 1 : 0),
                        mStartTime, mEndTime, TimerRepeatMode.ONCE);
                DVBManager.getInstance().createTimerRecord(params);
                Toast.makeText(mContext, R.string.record_created,
                        Toast.LENGTH_SHORT).show();
                return true;
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (InternalException e) {
                e.printStackTrace();
                Toast.makeText(mContext, R.string.create_record_failed,
                        Toast.LENGTH_SHORT).show();
            }
        }
        return false;
    }
}
