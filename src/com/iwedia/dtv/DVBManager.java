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

import android.content.Context;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.iwedia.activities.EPGActivity;
import com.iwedia.dtv.dtvmanager.DTVManager;
import com.iwedia.dtv.dtvmanager.IDTVManager;
import com.iwedia.dtv.epg.EpgEvent;
import com.iwedia.dtv.epg.EpgTimeFilter;
import com.iwedia.dtv.epg.IEpgCallback;
import com.iwedia.dtv.route.broadcast.IBroadcastRouteControl;
import com.iwedia.dtv.route.broadcast.RouteDemuxDescriptor;
import com.iwedia.dtv.route.broadcast.RouteFrontendDescriptor;
import com.iwedia.dtv.route.broadcast.RouteFrontendType;
import com.iwedia.dtv.route.common.ICommonRouteControl;
import com.iwedia.dtv.route.common.RouteDecoderDescriptor;
import com.iwedia.dtv.route.common.RouteInputOutputDescriptor;
import com.iwedia.dtv.service.IServiceControl;
import com.iwedia.dtv.service.ServiceDescriptor;
import com.iwedia.dtv.service.SourceType;
import com.iwedia.dtv.types.InternalException;
import com.iwedia.dtv.types.TimeDate;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;

/**
 * DVBManager - Class For Handling MW Components.
 */
public class DVBManager {
    public static final String TAG = "DVBManager";
    /** Date Format */
    public static final String DATE_FORMAT = "HH:mm:ss' 'dd/MM/yyyy";
    private Context mContext = null;
    private IDTVManager mDTVManager = null;
    private int mCurrentLiveRoute = 0;
    private int mLiveRouteSat = 0;
    private int mLiveRouteTer = 0;
    private int mLiveRouteCab = 0;
    private int mLiveRouteIp = 0;
    /** Currently active list in comedia. */
    private int mCurrentListIndex = 0;
    /** Holder who is holding loaded epg events */
    private ArrayList<TimeEvent> mTimeEventHolders = null;
    /** EPG Filter ID */
    private int mEpgFilterID = -1;

    /** Flag that indicates if loading is in progress. */
    private boolean loadInProgress = false;

    /** Listener for loading EPG events */
    public interface OnLoadFinishedListener {
        public void onLoadFinished();
    }

    public DVBManager(Context context) {
        mContext = context;
        mDTVManager = new DTVManager();
    }

    /**
     * Initialize Service.
     * 
     * @throws RemoteException
     */
    public void InitializeDTVService() {
        initializeRouteId();
        try {
            mEpgFilterID = mDTVManager.getEpgControl().createEventList();
            Log.d(TAG, "mEpgFilterID=" + mEpgFilterID);
        } catch (InternalException e) {
            e.printStackTrace();
        }
    }

    /**
     * Initialize Descriptors For Live Route.
     */
    private void initializeRouteId() {
        IBroadcastRouteControl broadcastRouteControl = mDTVManager
                .getBroadcastRouteControl();
        ICommonRouteControl commonRouteControl = mDTVManager
                .getCommonRouteControl();
        /**
         * RETRIEVE DEMUX DESCRIPTOR.
         */
        RouteDemuxDescriptor demuxDescriptor = broadcastRouteControl
                .getDemuxDescriptor(0);
        /**
         * RETRIEVE DECODER DESCRIPTOR.
         */
        RouteDecoderDescriptor decoderDescriptor = commonRouteControl
                .getDecoderDescriptor(0);
        /**
         * RETRIEVING OUTPUT DESCRIPTOR.
         */
        RouteInputOutputDescriptor outputDescriptor = commonRouteControl
                .getInputOutputDescriptor(0);
        /**
         * GET NUMBER OF FRONTENDS.
         */
        int numberOfFrontends = broadcastRouteControl.getFrontendNumber();
        /**
         * FIND DVB and IP front-end descriptors.
         */
        EnumSet<RouteFrontendType> frontendTypes = null;
        for (int i = 0; i < numberOfFrontends; i++) {
            RouteFrontendDescriptor frontendDescriptor = broadcastRouteControl
                    .getFrontendDescriptor(i);
            frontendTypes = frontendDescriptor.getFrontendType();
            for (RouteFrontendType frontendType : frontendTypes) {
                switch (frontendType) {
                    case SAT: {
                        if (mLiveRouteSat == 0) {
                            mLiveRouteSat = getLiveRouteId(frontendDescriptor,
                                    demuxDescriptor, decoderDescriptor,
                                    outputDescriptor, broadcastRouteControl);
                        }
                        break;
                    }
                    case CAB: {
                        if (mLiveRouteCab == 0) {
                            mLiveRouteCab = getLiveRouteId(frontendDescriptor,
                                    demuxDescriptor, decoderDescriptor,
                                    outputDescriptor, broadcastRouteControl);
                        }
                        break;
                    }
                    case TER: {
                        if (mLiveRouteTer == 0) {
                            mLiveRouteTer = getLiveRouteId(frontendDescriptor,
                                    demuxDescriptor, decoderDescriptor,
                                    outputDescriptor, broadcastRouteControl);
                        }
                        break;
                    }
                    case IP: {
                        if (mLiveRouteIp == 0) {
                            mLiveRouteIp = getLiveRouteId(frontendDescriptor,
                                    demuxDescriptor, decoderDescriptor,
                                    outputDescriptor, broadcastRouteControl);
                        }
                        break;
                    }
                    default:
                        break;
                }
            }
        }
    }

    /**
     * Get Live Route From Descriptors.
     * 
     * @param fDescriptor
     * @param mDemuxDescriptor
     * @param mDecoderDescriptor
     * @param mOutputDescriptor
     */
    private int getLiveRouteId(RouteFrontendDescriptor fDescriptor,
            RouteDemuxDescriptor mDemuxDescriptor,
            RouteDecoderDescriptor mDecoderDescriptor,
            RouteInputOutputDescriptor mOutputDescriptor,
            IBroadcastRouteControl routeControl) {
        return routeControl.getLiveRoute(fDescriptor.getFrontendId(),
                mDemuxDescriptor.getDemuxId(),
                mDecoderDescriptor.getDecoderId());
    }

    /**
     * Start MW video playback.
     * 
     * @throws IllegalArgumentException
     * @throws InternalException
     */
    public void startDTV(int channelNumber) throws IllegalArgumentException,
            InternalException {
        if (channelNumber < 0 || channelNumber >= getChannelListSize()) {
            throw new IllegalArgumentException("Illegal channel index!");
        }
        ServiceDescriptor desiredService = mDTVManager.getServiceControl()
                .getServiceDescriptor(mCurrentListIndex, channelNumber);
        int route = getActiveRouteByServiceType(desiredService.getSourceType());
        if (route == 0) {
            Toast.makeText(mContext, "Undefined channel type!",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        mCurrentLiveRoute = route;
        mDTVManager.getServiceControl().startService(route, mCurrentListIndex,
                channelNumber);
    }

    /**
     * Stop MW video playback.
     * 
     * @throws InternalException
     */
    public void stopDTV() throws InternalException {
        mDTVManager.getEpgControl().releaseEventList(mEpgFilterID);
        ServiceDescriptor desiredService = mDTVManager.getServiceControl()
                .getServiceDescriptor(mCurrentListIndex,
                        getCurrentChannelNumber());
        int route = getActiveRouteByServiceType(desiredService.getSourceType());
        if (route == 0) {
            Toast.makeText(mContext, "Undefined channel type!",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        mDTVManager.getServiceControl().stopService(route);
    }

    /**
     * Change Channel Up.
     * 
     * @return Channel Info Object.
     * @throws RemoteException
     *         If connection error happens.
     * @throws InternalException
     * @throws IllegalArgumentException
     */
    public ChannelInfo changeChannelUp() throws IllegalArgumentException,
            InternalException {
        int currentChannelNumber = getCurrentChannelNumber();
        int newChannelNumber = (currentChannelNumber + 1)
                % (getChannelListSize());
        return changeChannelByNumber(newChannelNumber);
    }

    /**
     * Change Channel Down.
     * 
     * @return Channel Info Object
     * @throws InternalException
     * @throws IllegalArgumentException
     */
    public ChannelInfo changeChannelDown() throws IllegalArgumentException,
            InternalException {
        int currentChannelNumber = getCurrentChannelNumber();
        int listSize = getChannelListSize();
        currentChannelNumber = (--currentChannelNumber + listSize) % listSize;
        return changeChannelByNumber(currentChannelNumber);
    }

    /**
     * Change Channel by Number.
     * 
     * @return Channel Info Object or null if error occurred.
     * @throws InternalException
     *         ,IllegalArgumentException
     */
    public ChannelInfo changeChannelByNumber(int channelNumber)
            throws IllegalArgumentException, InternalException {
        if (channelNumber < 0 || channelNumber >= getChannelListSize()) {
            throw new IllegalArgumentException("Illegal channel index!");
        }
        ServiceDescriptor desiredService = mDTVManager.getServiceControl()
                .getServiceDescriptor(mCurrentListIndex, channelNumber);
        int route = getActiveRouteByServiceType(desiredService.getSourceType());
        if (route == 0) {
            Toast.makeText(mContext, "Undefined channel type!",
                    Toast.LENGTH_SHORT).show();
            return null;
        }
        Log.i(TAG, "Route: " + route + " ChannelNumber: " + channelNumber);
        mCurrentLiveRoute = route;
        mDTVManager.getServiceControl().startService(route, mCurrentListIndex,
                channelNumber);
        return getChannelInfo(channelNumber);
    }

    /**
     * Return route by service type.
     * 
     * @param serviceType
     *        Service type to check.
     * @return Desired route, or 0 if service type is undefined.
     */
    private int getActiveRouteByServiceType(SourceType sourceType) {
        switch (sourceType) {
            case CAB: {
                return mLiveRouteCab;
            }
            case TER: {
                return mLiveRouteTer;
            }
            case SAT: {
                return mLiveRouteSat;
            }
            case IP: {
                return mLiveRouteIp;
            }
            default:
                return 0;
        }
    }

    /**
     * Get Size of Channel List.
     */
    public int getChannelListSize() {
        return mDTVManager.getServiceControl().getServiceListCount(
                mCurrentListIndex);
    }

    /**
     * Get Channel Names.
     */
    public ArrayList<String> getChannelNames() {
        ArrayList<String> channelNames = new ArrayList<String>();
        String channelName = "";
        int channelListSize = getChannelListSize();
        IServiceControl serviceControl = mDTVManager.getServiceControl();
        for (int i = 0; i < channelListSize; i++) {
            channelName = serviceControl.getServiceDescriptor(0, i).getName();
            channelNames.add(i, channelName);
        }
        return channelNames;
    }

    /**
     * Get Current Channel Number.
     */
    public int getCurrentChannelNumber() {
        return mDTVManager.getServiceControl()
                .getActiveService(mCurrentLiveRoute).getServiceIndex();
    }

    /**
     * Get Current Channel Number and Channel Name.
     * @return Object of Channel Info class.
     * @throws IllegalArgumentException
     */
    public ChannelInfo getChannelInfo(int channelNumber)
            throws IllegalArgumentException {
        if (channelNumber < 0 || channelNumber >= getChannelListSize()) {
            throw new IllegalArgumentException("Illegal channel index!");
        }
        String channelName = "";
        channelName = mDTVManager.getServiceControl()
                .getServiceDescriptor(mCurrentListIndex, channelNumber)
                .getName();
        return new ChannelInfo(channelNumber + 1, channelName);
    }

    /**
     * Load Events From MW
     * 
     * @param channelListSize
     *        Number of services in channel list
     * @throws ParseException
     * @throws RemoteException
     */
    public void loadEvents(OnLoadFinishedListener listener)
            throws ParseException, IllegalArgumentException {
        if (listener == null) {
            throw new IllegalArgumentException("LoadFinishedListener is NULL");
        }
        if (!loadInProgress) {
            loadInProgress = true;
            // SimpleDateFormat lDateFormat = new SimpleDateFormat(DATE_FORMAT);
            Date lBeginTime = null;
            Date lEndTime = null;
            Date lParsedBeginTime = null;
            Date lParsedEndTime = null;
            EpgEvent lEvent = null;
            int lEpgEventsSize = 0;
            int lEventDay = -1;
            TimeDate lCurrentTime = mDTVManager.getSetupControl().getTimeDate();
            TimeDate lEpgStartTime = new TimeDate(0, 0, 0,
                    lCurrentTime.getDay(), lCurrentTime.getMonth(),
                    lCurrentTime.getYear());
            TimeDate lEpgEndTime = new TimeDate(0, 59, 23,
                    lCurrentTime.getDay(), lCurrentTime.getMonth(),
                    lCurrentTime.getYear());
            mTimeEventHolders = new ArrayList<TimeEvent>();
            for (int i = 0; i < EPGActivity.HOURS; i++) {
                mTimeEventHolders.add(new TimeEvent(getChannelListSize()));
            }

            /** Reset Filter */
            // mDTVManager.getEpgControl().releaseEventList(mEpgFilterID);
            mDTVManager.getEpgControl().startAcquisition(mEpgFilterID);
            /** Create Time Filter */
            EpgTimeFilter lEpgTimeFilter = new EpgTimeFilter();
            lEpgTimeFilter.setTime(lEpgStartTime, lEpgEndTime);
            /** Make filter list by time */
            // mDTVManager.getEpgControl().setFilter(mEpgFilterID,
            // lEpgTimeFilter);
            for (int channelIndex = 0; channelIndex < getChannelListSize(); channelIndex++) {
                lEpgEventsSize = mDTVManager.getEpgControl()
                        .getAvailableEventsNumber(mEpgFilterID, channelIndex);
                for (int eventIndex = 0; eventIndex < lEpgEventsSize; eventIndex++) {
                    lEvent = mDTVManager.getEpgControl().getRequestedEvent(
                            mEpgFilterID, channelIndex, eventIndex);
                    lBeginTime = lEvent.getStartTime().getCalendar().getTime();
                    lEndTime = lEvent.getEndTime().getCalendar().getTime();
                    // lDateFormat.parse(lEvent.getStartTime().toString());
                    // lEndTime =
                    // lDateFormat.parse(lEvent.getEndTime().toString());
                    if (lEventDay == -1) {
                        lEventDay = lBeginTime.getDay();
                    }
                    if (lBeginTime.getDay() == lEventDay) {
                        if (lEndTime.getDay() != lEventDay) {
                            lEndTime = new Date(lBeginTime.getYear(),
                                    lBeginTime.getMonth(), lBeginTime.getDay(),
                                    lBeginTime.getHours(), 59, 0);
                        }
                        if (lBeginTime.getHours() < lEndTime.getHours()) {
                            lParsedEndTime = new Date(lBeginTime.getYear(),
                                    lBeginTime.getMonth(), lBeginTime.getDay(),
                                    lBeginTime.getHours(), 59, 0);
                            mTimeEventHolders.get(lBeginTime.getHours())
                                    .addEvent(channelIndex, lEvent.getName(),
                                            lBeginTime, lParsedEndTime);
                            lParsedBeginTime = new Date(lEndTime.getYear(),
                                    lEndTime.getMonth(), lEndTime.getDay(),
                                    lEndTime.getHours(), 0, 0);
                            mTimeEventHolders.get(lEndTime.getHours())
                                    .addEvent(channelIndex, lEvent.getName(),
                                            lParsedBeginTime, lEndTime);
                        } else {
                            mTimeEventHolders.get(lBeginTime.getHours())
                                    .addEvent(channelIndex, lEvent.getName(),
                                            lBeginTime, lEndTime);
                        }
                    }
                }
            }
            mDTVManager.getEpgControl().stopAcquisition(mEpgFilterID);
            loadInProgress = false;
            listener.onLoadFinished();
        }
    }

    /**
     * Return Loaded EPG Events holder
     * 
     * @return Populated events holder
     */
    public ArrayList<TimeEvent> getLoadedEpgEvents() {
        return mTimeEventHolders;
    }

    /**
     * Set events callback
     * 
     * @param eventCallback
     *        Event Callback
     * @throws RemoteException
     */
    public void setEventCallback(IEpgCallback eventCallback) {
        mDTVManager.getEpgControl().registerCallback(eventCallback,
                mEpgFilterID);
    }

    public void removeEventCAllback(IEpgCallback eventCallback) {
        mDTVManager.getEpgControl().unregisterCallback(eventCallback,
                mEpgFilterID);
    }
}
