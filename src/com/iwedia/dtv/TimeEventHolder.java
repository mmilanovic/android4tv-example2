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

import com.iwedia.dtv.epg.EpgEvent;

import java.util.Date;

/**
 * TimeEventHolder keeps important information about an event.
 */
public class TimeEventHolder {
    private String mEventName = "";
    private Date mBeginTime = null;
    private Date mEndTime = null;
    private String mDuration = "";
    private String mDescription = "";
    private String mParentalRaiting = "";
    private String mGenre = "";
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
    public TimeEventHolder(String eventName, Date beginTime, Date endTime,
            String description, String parentalRaiting, String genre,
            EpgEvent event) {
        mEventName = eventName;
        mBeginTime = beginTime;
        mEndTime = endTime;
        mDescription = description;
        mParentalRaiting = parentalRaiting;
        mGenre = genre;
        mEvent = event;
        calculateDuration();
    }

    private void calculateDuration() {
        long lBeginTime = mBeginTime.getTime();
        long lEndTime = mEndTime.getTime();
        long lDuration = lEndTime - lBeginTime;
        Date lDateDuration = new Date(lDuration);
        mDuration = lDateDuration.getHours() + ":" + lDateDuration.getMinutes();
    }

    public String getEventName() {
        return mEventName;
    }

    public Date getBeginTime() {
        return mBeginTime;
    }

    public Date getEndTime() {
        return mEndTime;
    }

    public EpgEvent getEvent() {
        return mEvent;
    }

    @Override
    public String toString() {
        return "EventName: " + mEventName + "\n\n StartTime: "
                + mBeginTime.getHours() + ":" + mBeginTime.getMinutes()
                + "\n\n EndTime: " + mEndTime.getHours() + ":"
                + mEndTime.getMinutes() + "\n\n Duration: " + mDuration + "\n"
                + "\n Extended Description: " + mDescription + "\n"
                + "\n Parental Rating: " + mParentalRaiting + "\n"
                + "\n Genre: " + mGenre + "\n";
    }
}
