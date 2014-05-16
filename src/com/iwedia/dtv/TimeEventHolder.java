/*
 * Copyright (C) 2014 iWedia S.A.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.iwedia.dtv;

import java.util.Date;

/**
 * TimeEventHolder keeps important information about an event.
 */
public class TimeEventHolder {
    private String mEventName = "";
    private Date mBeginTime = null;
    private Date mEndTime = null;

    /**
     * Create Holder
     * @param eventName
     *        Event name.
     * @param beginTime
     *        Time when event begins.
     * @param endTime
     *        Time when event ends.
     */
    public TimeEventHolder(String eventName, Date beginTime, Date endTime) {
        mEventName = eventName;
        mBeginTime = beginTime;
        mEndTime = endTime;
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

    @Override
    public String toString() {
        return "EventName: " + mEventName + " StartTime: "
                + mBeginTime.getHours() + ":" + mBeginTime.getMinutes()
                + " EndTime: " + mEndTime.getHours() + ":"
                + mEndTime.getMinutes();
    }
}
