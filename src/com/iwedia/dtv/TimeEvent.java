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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

/**
 * TimeEvent keeps all events for all channels for one hour.
 */
public class TimeEvent {
    private ArrayList<ArrayList<TimeEventHolder>> mChannelHolder = null;

    /**
     * Create a TimeEvent
     * 
     * @param channelListSize
     *        Size of channel list.
     */
    public TimeEvent(int channelListSize) {
        mChannelHolder = new ArrayList<ArrayList<TimeEventHolder>>();
        for (int i = 0; i < channelListSize; i++) {
            mChannelHolder.add(i, new ArrayList<TimeEventHolder>());
        }
    }

    /**
     * Add Event for specific channel.
     * 
     * @param channel
     *        Channel index.
     * @param eventName
     *        Name of the event.
     * @param beginTime
     *        Begin time of the event;
     * @param endTime
     *        End time of the event;
     * @throws ParseException
     */
    public void addEvent(int channel, Date beginTime, Date endTime,
            EpgEvent event) throws ParseException {
        mChannelHolder.get(channel).add(
                new TimeEventHolder(beginTime, endTime, event));
    }

    public ArrayList<ArrayList<TimeEventHolder>> getEvents() {
        return mChannelHolder;
    }
}
