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

import com.iwedia.activities.EPGActivity;
import com.iwedia.dtv.epg.EpgEvent;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;

/**
 * TimeEventHolder keeps important information about an event.
 */
public class TimeEventHolder {
    private Date mDrawingBeginTime = null;
    private Date mDrawingEndTime = null;
    private String mDuration = "";
    private EpgEvent mEvent;

    /**
     * Create Holder
     * 
     * @param eventName
     *        Event name.
     * @param beginTime
     *        Time when event begins.
     * @param endTime
     *        Time when event ends.
     */
    public TimeEventHolder(Date drawingBeginTime, Date drawingEndTime,
            EpgEvent event) {
        mDrawingBeginTime = drawingBeginTime;
        mDrawingEndTime = drawingEndTime;
        mEvent = event;
        calculateDuration();
    }

    private void calculateDuration() {
        long lBeginTime = mEvent.getStartTime().getCalendar().getTimeInMillis();
        long lEndTime = mEvent.getEndTime().getCalendar().getTimeInMillis();
        long lDuration = lEndTime - lBeginTime;
        Date lDateDuration = new Date(lDuration);
        mDuration = lDateDuration.getHours() + ":"
                + String.format("%02d", lDateDuration.getMinutes());
    }

    public String getEventName() {
        return mEvent.getName();
    }

    public Date getBeginTime() {
        return mDrawingBeginTime;
    }

    public Date getEndTime() {
        return mDrawingEndTime;
    }

    public EpgEvent getEvent() {
        return mEvent;
    }

    @Override
    public String toString() {
        return "EventName: " + getEventName() + "\n\n StartTime: "
                + mEvent.getStartTime().getHour() + ":"
                + String.format("%02d", mEvent.getStartTime().getMin())
                + "\n\n EndTime: " + mEvent.getEndTime().getHour() + ":"
                + String.format("%02d", mEvent.getEndTime().getMin())
                + "\n\n Duration: " + mDuration + "\n"
                + "\n Extended Description: " + mEvent.getDescription() + "\n"
                + "\n Parental Rating: "
                + EPGActivity.getParentalRating(mEvent.getParentalRate())
                + "\n" + "\n Genre: "
                + EPGActivity.getEPGGenre(mEvent.getGenre()) + "\n";
    }
}
