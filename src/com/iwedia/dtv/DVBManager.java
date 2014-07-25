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

import android.os.RemoteException;
import android.util.Log;

import com.iwedia.activities.DTVActivity;
import com.iwedia.activities.EPGActivity;
import com.iwedia.callback.EPGCallBack;
import com.iwedia.callback.PvrCallback;
import com.iwedia.callback.ReminderCallback;
import com.iwedia.dtv.dtvmanager.DTVManager;
import com.iwedia.dtv.dtvmanager.IDTVManager;
import com.iwedia.dtv.epg.EpgEvent;
import com.iwedia.dtv.epg.EpgEventGenre;
import com.iwedia.dtv.epg.EpgGenreFilter;
import com.iwedia.dtv.epg.EpgServiceFilter;
import com.iwedia.dtv.epg.EpgTimeFilter;
import com.iwedia.dtv.pvr.SmartCreateParams;
import com.iwedia.dtv.pvr.TimerCreateParams;
import com.iwedia.dtv.reminder.ReminderSmartParam;
import com.iwedia.dtv.reminder.ReminderTimerParam;
import com.iwedia.dtv.route.broadcast.IBroadcastRouteControl;
import com.iwedia.dtv.route.broadcast.RouteDemuxDescriptor;
import com.iwedia.dtv.route.broadcast.RouteFrontendDescriptor;
import com.iwedia.dtv.route.broadcast.RouteFrontendType;
import com.iwedia.dtv.route.broadcast.RouteMassStorageDescriptor;
import com.iwedia.dtv.route.common.ICommonRouteControl;
import com.iwedia.dtv.route.common.RouteDecoderDescriptor;
import com.iwedia.dtv.route.common.RouteInputOutputDescriptor;
import com.iwedia.dtv.service.IServiceControl;
import com.iwedia.dtv.service.ServiceDescriptor;
import com.iwedia.dtv.service.SourceType;
import com.iwedia.dtv.swupdate.SWVersionType;
import com.iwedia.dtv.types.InternalException;
import com.iwedia.dtv.types.TimeDate;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;

/**
 * DVBManager - Class For Handling MW Components.
 */
public class DVBManager {
    public static final String TAG = "DVBManager";
    /** Date Format */
    public static final String DATE_FORMAT = "HH:mm:ss' 'dd/MM/yyyy";
    /** 7 DAYS EPG */
    private static final int MAX_EPG_DAYS = 6;
    public static final int LOAD_EPG_PREVIOUS_DAY = -1;
    public static final int LOAD_EPG_CURRENT_DAY = 0;
    public static final int LOAD_EPG_NEXT_DAY = 1;
    private IDTVManager mDTVManager = null;
    private int mCurrentLiveRoute = -1;
    private int mLiveRouteSat = -1;
    private int mLiveRouteTer = -1;
    private int mLiveRouteCab = -1;
    private int mLiveRouteIp = -1;
    private int mPlaybackRouteIDMain = -1;
    private int mRecordRouteTer = -1;
    private int mRecordRouteCab = -1;
    private int mRecordRouteSat = -1;
    private int mRecordRouteIp = -1;
    private int mCurrentRecordRoute = -1;
    /** Currently active list in Comedia. */
    private int mCurrentListIndex = 0;
    /** IP stuff */
    private int mCurrentChannelNumberIp = -1;
    private boolean ipAndSomeOtherTunerType = false;
    /** DVB Manager Instance. */
    private static DVBManager sInstance = null;
    /** Holder who is holding loaded EPG events. */
    private TimeEvent[] mTimeEventHolders = null;
    /** EPG Filter ID */
    private int mEPGFilterID = -1;
    /** EPG CallBack */
    private EPGCallBack mEPGCallBack = null;
    /** Flag that indicates if loading is in progress. */
    private boolean loadInProgress = false;
    /** EPG Events Loaded */
    private OnLoadFinishedListener mLoadFinishedListener = null;
    /** CallBack for UI. */
    private DVBStatus mDVBStatus = null;
    /** EPG Current Day. */
    private int mEPGDay = 0;
    /** Active EPG genre */
    private EpgEventGenre mGenre = EpgEventGenre.GENRE_ALL;
    private PvrManager mPvrManager = null;
    /** Reminder manager */
    private ReminderManager mReminderManager = null;

    /**
     * CallBack for currently DVB status.
     */
    public interface DVBStatus {
        /** Alert UI that channel is scrambled. */
        public void channelIsScrambled();
    }

    /**
     * Listener for loading EPG events
     */
    public interface OnLoadFinishedListener {
        public void onLoadFinished(String date);
    }

    public static DVBManager getInstance() throws InternalException {
        if (sInstance == null) {
            sInstance = new DVBManager();
        }
        return sInstance;
    }

    private DVBManager() throws InternalException {
        mDTVManager = new DTVManager();
        InitializeDTVService();
    }

    /**
     * Initialize Service.
     * 
     * @throws InternalException
     */
    public void InitializeDTVService() throws InternalException {
        initializeRouteId();
        mReminderManager = ReminderManager.getInstance(mDTVManager);
        mPvrManager = PvrManager.getInstance(mDTVManager);
        mEPGFilterID = mDTVManager.getEpgControl().createEventList();
        mEPGCallBack = new EPGCallBack(this);
        mDTVManager.getEpgControl()
                .registerCallback(mEPGCallBack, mEPGFilterID);
        /** Initially set genre to ALL */
        setGenreFilter(EpgEventGenre.GENRE_ALL);
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
         * RETRIEVING MASS STORAGE DESCRIPTOR.
         */
        RouteMassStorageDescriptor massStorageDescriptor = new RouteMassStorageDescriptor();
        massStorageDescriptor = broadcastRouteControl
                .getMassStorageDescriptor(0);
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
                        if (mLiveRouteSat == -1) {
                            mLiveRouteSat = getLiveRouteId(frontendDescriptor,
                                    demuxDescriptor, decoderDescriptor,
                                    outputDescriptor, broadcastRouteControl);
                        }
                        /**
                         * RETRIEVE RECORD ROUTES
                         */
                        if (mRecordRouteSat == -1) {
                            mRecordRouteSat = broadcastRouteControl
                                    .getRecordRoute(frontendDescriptor
                                            .getFrontendId(), demuxDescriptor
                                            .getDemuxId(),
                                            massStorageDescriptor
                                                    .getMassStorageId());
                        }
                        break;
                    }
                    case CAB: {
                        if (mLiveRouteCab == -1) {
                            mLiveRouteCab = getLiveRouteId(frontendDescriptor,
                                    demuxDescriptor, decoderDescriptor,
                                    outputDescriptor, broadcastRouteControl);
                        }
                        /**
                         * RETRIEVE RECORD ROUTES
                         */
                        if (mRecordRouteCab == -1) {
                            mRecordRouteCab = broadcastRouteControl
                                    .getRecordRoute(frontendDescriptor
                                            .getFrontendId(), demuxDescriptor
                                            .getDemuxId(),
                                            massStorageDescriptor
                                                    .getMassStorageId());
                        }
                        break;
                    }
                    case TER: {
                        if (mLiveRouteTer == -1) {
                            mLiveRouteTer = getLiveRouteId(frontendDescriptor,
                                    demuxDescriptor, decoderDescriptor,
                                    outputDescriptor, broadcastRouteControl);
                        }
                        /**
                         * RETRIEVE RECORD ROUTES
                         */
                        if (mRecordRouteTer == -1) {
                            mRecordRouteTer = broadcastRouteControl
                                    .getRecordRoute(frontendDescriptor
                                            .getFrontendId(), demuxDescriptor
                                            .getDemuxId(),
                                            massStorageDescriptor
                                                    .getMassStorageId());
                        }
                        break;
                    }
                    case IP: {
                        if (mLiveRouteIp == -1) {
                            mLiveRouteIp = getLiveRouteId(frontendDescriptor,
                                    demuxDescriptor, decoderDescriptor,
                                    outputDescriptor, broadcastRouteControl);
                        }
                        /**
                         * RETRIEVE RECORD ROUTES
                         */
                        if (mRecordRouteIp == -1) {
                            mRecordRouteIp = broadcastRouteControl
                                    .getRecordRoute(frontendDescriptor
                                            .getFrontendId(), demuxDescriptor
                                            .getDemuxId(),
                                            massStorageDescriptor
                                                    .getMassStorageId());
                        }
                        break;
                    }
                    default:
                        break;
                }
            }
        }
        /**
         * RETRIEVE PLAYBACK ROUTE
         */
        mPlaybackRouteIDMain = broadcastRouteControl.getPlaybackRoute(
                massStorageDescriptor.getMassStorageId(),
                demuxDescriptor.getDemuxId(), decoderDescriptor.getDecoderId());
        if (mLiveRouteIp != -1
                && (mLiveRouteCab != -1 || mLiveRouteSat != -1 || mLiveRouteTer != -1)) {
            ipAndSomeOtherTunerType = true;
        }
        Log.d(TAG, "mLiveRouteTer=" + mLiveRouteTer + ", mLiveRouteCab="
                + mLiveRouteCab + ", mLiveRouteIp=" + mLiveRouteIp);
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
     * Stop MW video playback.
     * 
     * @throws InternalException
     */
    public void stopDTV() throws InternalException {
        mPvrManager.unregisterPvrCallback();
        mReminderManager.unregisterCallback();
        mDTVManager.getEpgControl().releaseEventList(mEPGFilterID);
        mDTVManager.getEpgControl().unregisterCallback(mEPGCallBack,
                mEPGFilterID);
        PvrCallback.destroyInstance();
        ReminderCallback.destroyInstance();
        mDTVManager.getServiceControl().stopService(mCurrentLiveRoute);
        sInstance = null;
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
        int listSize = getChannelListSize();
        if (listSize == 0) {
            return null;
        }
        return changeChannelByNumber((getCurrentChannelNumber() + 1) % listSize);
    }

    /**
     * Change Channel Down.
     * 
     * @return Channel Info Object.
     * @throws InternalException
     * @throws IllegalArgumentException
     */
    public ChannelInfo changeChannelDown() throws IllegalArgumentException,
            InternalException {
        int currentChannelNumber = getCurrentChannelNumber();
        int listSize = getChannelListSize();
        if (listSize == 0) {
            return null;
        }
        return changeChannelByNumber((--currentChannelNumber + listSize)
                % listSize);
    }

    /**
     * Change Channel by Number.
     * 
     * @return Channel Info Object or null if error occurred.
     * @throws IllegalArgumentException
     * @throws InternalException
     */
    public ChannelInfo changeChannelByNumber(int channelNumber)
            throws InternalException {
        int listSize = getChannelListSize();
        if (listSize == 0) {
            return null;
        }
        channelNumber = (channelNumber + listSize) % listSize;
        int numberOfDtvChannels = listSize
                - (mLiveRouteIp == -1 ? 0 : DTVActivity.sIpChannels.size());
        /** For regular DVB channel. */
        if (channelNumber < numberOfDtvChannels) {
            ServiceDescriptor desiredService = mDTVManager.getServiceControl()
                    .getServiceDescriptor(
                            mCurrentListIndex,
                            ipAndSomeOtherTunerType ? channelNumber + 1
                                    : channelNumber);
            /** Channel is Scrambled Toast. */
            if (desiredService.isScrambled()) {
                mDVBStatus.channelIsScrambled();
            }
            int route = getActiveRouteByServiceType(desiredService
                    .getSourceType());
            if (route == -1) {
                return null;
            }
            mCurrentLiveRoute = route;
            mDTVManager.getServiceControl()
                    .startService(
                            route,
                            mCurrentListIndex,
                            ipAndSomeOtherTunerType ? channelNumber + 1
                                    : channelNumber);
        }
        /** For IP. */
        else {
            mCurrentLiveRoute = mLiveRouteIp;
            mCurrentChannelNumberIp = channelNumber;
            mDTVManager.getServiceControl().zapURL(
                    mLiveRouteIp,
                    DTVActivity.sIpChannels.get(
                            channelNumber - numberOfDtvChannels).getUrl());
        }
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
                return -1;
        }
    }

    /**
     * Return record route by service type
     * 
     * @param sourceType
     *        Service type to check.
     * @return Desired route, or -1 if service type is undefined.
     */
    private int getActiveRecordRouteByServiceType(SourceType sourceType) {
        switch (sourceType) {
            case CAB: {
                return mRecordRouteCab;
            }
            case TER: {
                return mRecordRouteTer;
            }
            case SAT: {
                return mRecordRouteSat;
            }
            case IP: {
                return mRecordRouteIp;
            }
            default:
                return -1;
        }
    }

    /**
     * Get Size of Channel List.
     */
    public int getChannelListSize() {
        int serviceCount = mDTVManager.getServiceControl().getServiceListCount(
                mCurrentListIndex);
        if (ipAndSomeOtherTunerType) {
            serviceCount += DTVActivity.sIpChannels == null ? 0
                    : DTVActivity.sIpChannels.size();
            serviceCount--;
        } else
        /** Only IP. */
        if (mLiveRouteIp != -1) {
            serviceCount = DTVActivity.sIpChannels == null ? 0
                    : DTVActivity.sIpChannels.size();
        }
        return serviceCount;
    }

    /**
     * Get Channel Names.
     */
    public ArrayList<String> getChannelNames() {
        ArrayList<String> channelNames = new ArrayList<String>();
        String channelName = "";
        int channelListSize = getChannelListSize()
                - (mLiveRouteIp == -1 ? 0 : DTVActivity.sIpChannels.size());
        IServiceControl serviceControl = mDTVManager.getServiceControl();
        /** If there is IP first element in service list is DUMMY. */
        channelListSize = ipAndSomeOtherTunerType ? channelListSize + 1
                : channelListSize;
        for (int i = ipAndSomeOtherTunerType ? 1 : 0; i < channelListSize; i++) {
            channelName = serviceControl.getServiceDescriptor(
                    mCurrentListIndex, i).getName();
            channelNames.add(channelName);
        }
        /** Add IP. */
        if (mLiveRouteIp != -1) {
            for (int i = 0; i < DTVActivity.sIpChannels.size(); i++) {
                channelNames.add(DTVActivity.sIpChannels.get(i).getName());
            }
        }
        return channelNames;
    }

    /**
     * Get Current Channel Number.
     */
    public int getCurrentChannelNumber() {
        /** For IP. */
        if (mCurrentLiveRoute == mLiveRouteIp) {
            return mCurrentChannelNumberIp;
        }
        return (int) (mDTVManager.getServiceControl().getActiveService(
                mCurrentLiveRoute).getServiceIndex())
                - (ipAndSomeOtherTunerType ? 1 : 0);
    }

    /**
     * Get Current Channel Number and Channel Name.
     * 
     * @return Object of Channel Info class.
     * @throws IllegalArgumentException
     */
    public ChannelInfo getChannelInfo(int channelNumber)
            throws IllegalArgumentException {
        if (channelNumber < 0 || channelNumber >= getChannelListSize()) {
            throw new IllegalArgumentException("Illegal channel index! "
                    + channelNumber + ", List size is: " + getChannelListSize());
        }
        int numberOfDtvChannels = getChannelListSize()
                - (mLiveRouteIp == -1 ? 0 : DTVActivity.sIpChannels.size());
        /** Return DTV channel. */
        if (channelNumber < numberOfDtvChannels) {
            String channelName = mDTVManager
                    .getServiceControl()
                    .getServiceDescriptor(
                            mCurrentListIndex,
                            ipAndSomeOtherTunerType ? channelNumber + 1
                                    : channelNumber).getName();
            return new ChannelInfo(channelNumber + 1, channelName);
        }
        /** Return IP channel. */
        else {
            return new ChannelInfo(channelNumber + 1, DTVActivity.sIpChannels
                    .get(channelNumber - numberOfDtvChannels).getName());
        }
    }

    public void setGenreFilter(EpgEventGenre genre) {
        mGenre = genre;
    }

    /**
     * Load Events From MW.
     * 
     * @param day
     *        -Load EPG for previous or current or next day.
     * @param channelListSize
     *        Number of services in channel list
     * @throws ParseException
     * @throws RemoteException
     */
    public synchronized void loadEvents(int day) throws ParseException {
        loadInProgress = true;
        Date lDrawingBeginTime = null;
        Date lDrawingEndTime = null;
        Date lDrawingParsedBeginTime = null;
        Date lDrawingParsedEndTime = null;
        EpgEvent lEvent = null;
        int lEpgEventsSize = 0;
        int lEventDay = -1;
        TimeDate lCurrentTime = mDTVManager.getSetupControl().getTimeDate();
        switch (day) {
            case LOAD_EPG_PREVIOUS_DAY: {
                if (mEPGDay > 0) {
                    mEPGDay--;
                } else {
                    mEPGDay = 0;
                }
                break;
            }
            case LOAD_EPG_CURRENT_DAY: {
                /** Do Nothing, Don't Change Index. */
                break;
            }
            case LOAD_EPG_NEXT_DAY: {
                if (mEPGDay < MAX_EPG_DAYS) {
                    mEPGDay++;
                } else {
                    mEPGDay = MAX_EPG_DAYS;
                }
                break;
            }
        }
        Calendar lCalendar = lCurrentTime.getCalendar();
        lCalendar.add(Calendar.DATE, mEPGDay);
        TimeDate lEpgStartTime = new TimeDate(1, 1, 0,
                lCalendar.get(Calendar.DAY_OF_MONTH),
                lCalendar.get(Calendar.MONTH) + 1, lCalendar.get(Calendar.YEAR));
        TimeDate lEpgEndTime = new TimeDate(0, 0, 0,
                lCalendar.get(Calendar.DAY_OF_MONTH),
                lCalendar.get(Calendar.MONTH) + 1, lCalendar.get(Calendar.YEAR));
        mTimeEventHolders = new TimeEvent[EPGActivity.HOURS];
        for (int i = 0; i < EPGActivity.HOURS; i++) {
            mTimeEventHolders[i] = new TimeEvent(getChannelListSize());
        }
        /** Create Time Filter */
        EpgTimeFilter lEpgTimeFilter = new EpgTimeFilter();
        lEpgTimeFilter.setTime(lEpgStartTime, lEpgEndTime);
        /** Make filter list by time. */
        mDTVManager.getEpgControl().setFilter(mEPGFilterID, lEpgTimeFilter);
        /** Remove IP Channels, there is not EPG for that type. */
        int count = getChannelListSize()
                - (mLiveRouteIp == -1 ? 0 : DTVActivity.sIpChannels.size());
        for (int channelIndex = 0; channelIndex < count; channelIndex++) {
            /** Create Service Filter. */
            EpgServiceFilter lEpgServiceFilter = new EpgServiceFilter();
            lEpgServiceFilter
                    .setServiceIndex(ipAndSomeOtherTunerType ? channelIndex + 1
                            : channelIndex);
            /** Set Service Filter. */
            mDTVManager.getEpgControl().setFilter(mEPGFilterID,
                    lEpgServiceFilter);
            /** Create Genre Filter. */
            EpgGenreFilter genreFilter = new EpgGenreFilter();
            EnumSet<EpgEventGenre> set = EnumSet.of(mGenre);
            genreFilter.setGenre(set);
            /** Set Genre Filter. */
            mDTVManager.getEpgControl().setFilter(mEPGFilterID, genreFilter);
            /** Reset Filter */
            mDTVManager.getEpgControl().startAcquisition(mEPGFilterID);
            lEpgEventsSize = mDTVManager
                    .getEpgControl()
                    .getAvailableEventsNumber(
                            mEPGFilterID,
                            mDTVManager
                                    .getServiceControl()
                                    .getServiceDescriptor(
                                            mCurrentListIndex,
                                            ipAndSomeOtherTunerType ? channelIndex + 1
                                                    : channelIndex)
                                    .getMasterIndex());
            lEventDay = -1;
            for (int eventIndex = 0; eventIndex < lEpgEventsSize; eventIndex++) {
                lEvent = mDTVManager.getEpgControl().getRequestedEvent(
                        mEPGFilterID,
                        ipAndSomeOtherTunerType ? channelIndex + 1
                                : channelIndex, eventIndex);
                lDrawingBeginTime = lEvent.getStartTime().getCalendar()
                        .getTime();
                lDrawingEndTime = lEvent.getEndTime().getCalendar().getTime();
                if (lEventDay == -1) {
                    lEventDay = lDrawingBeginTime.getDay();
                }
                if (lDrawingBeginTime.getDay() == lEventDay) {
                    if (lDrawingEndTime.getDay() != lEventDay) {
                        lDrawingEndTime = new Date(lDrawingBeginTime.getYear(),
                                lDrawingBeginTime.getMonth(),
                                lDrawingBeginTime.getDay(),
                                lDrawingBeginTime.getHours(), 59, 0);
                    }
                    /** When duration of Event is a few hours.. */
                    if (lDrawingBeginTime.getHours() < lDrawingEndTime
                            .getHours()) {
                        for (int i = 0; i <= lDrawingEndTime.getHours()
                                - lDrawingBeginTime.getHours(); i++) {
                            /** If Event is longer than 1h. */
                            /** Draw from 'lDrawingBeginTime' to next hour. */
                            if (i == 0) {
                                lDrawingParsedBeginTime = lDrawingBeginTime;
                                lDrawingParsedEndTime = new Date(
                                        lDrawingBeginTime.getYear(),
                                        lDrawingBeginTime.getMonth(),
                                        lDrawingBeginTime.getDay(),
                                        lDrawingBeginTime.getHours() + 1, 0, 0);
                            }
                            /**
                             * Check is there need to draw end hour, if yes draw
                             * it from 'lDrawingEndTime.getHour():00' to
                             * 'lDrawingEndTime.getHour():lDrawingEndTime.getMin
                             * u t e s ( ) ' .
                             */
                            else if (i == lDrawingEndTime.getHours()
                                    - lDrawingBeginTime.getHours()) {
                                if (lDrawingEndTime.getMinutes() > 0) {
                                    lDrawingParsedBeginTime = new Date(
                                            lDrawingEndTime.getYear(),
                                            lDrawingEndTime.getMonth(),
                                            lDrawingEndTime.getDay(),
                                            lDrawingEndTime.getHours(), 0, 0);
                                    lDrawingParsedEndTime = lDrawingEndTime;
                                }
                            }
                            /** Draw other. */
                            else {
                                lDrawingParsedBeginTime = new Date(
                                        lDrawingBeginTime.getYear(),
                                        lDrawingBeginTime.getMonth(),
                                        lDrawingBeginTime.getDay(),
                                        lDrawingBeginTime.getHours() + i, 0, 0);
                                lDrawingParsedEndTime = new Date(
                                        lDrawingBeginTime.getYear(),
                                        lDrawingBeginTime.getMonth(),
                                        lDrawingBeginTime.getDay(),
                                        lDrawingBeginTime.getHours() + 1 + i,
                                        0, 0);
                            }
                            mTimeEventHolders[lDrawingParsedBeginTime
                                    .getHours()].addEvent(channelIndex,
                                    lDrawingParsedBeginTime,
                                    lDrawingParsedEndTime, lEvent);
                        }
                    } else {
                        /** Event in one hour column. */
                        mTimeEventHolders[lDrawingBeginTime.getHours()]
                                .addEvent(channelIndex, lDrawingBeginTime,
                                        lDrawingEndTime, lEvent);
                    }
                }
            }
            mDTVManager.getEpgControl().stopAcquisition(mEPGFilterID);
        }
        loadInProgress = false;
        if (mLoadFinishedListener != null) {
            mLoadFinishedListener.onLoadFinished("Display date: "
                    + lCalendar.get(Calendar.DAY_OF_MONTH) + "/"
                    + (lCalendar.get(Calendar.MONTH) + 1) + "/"
                    + lCalendar.get(Calendar.YEAR));
        }
    }

    /**
     * Reload EPG events,through thread.
     */
    public void reLoadEvents() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    loadEvents(DVBManager.LOAD_EPG_CURRENT_DAY);
                } catch (ParseException e) {
                    Log.e(TAG, "There was an error in reloading EPG events.", e);
                }
            }
        }).start();
    }

    public String getSwVersion(SWVersionType type) {
        return mDTVManager.getSoftwareUpdateControl().getSWVersion(type);
    }

    /**
     * Returns time from stream.
     */
    public TimeDate getCurrentTime() {
        return mDTVManager.getSetupControl().getTimeDate();
    }

    /**
     * Creates PVR smart record based on EPG event.
     * 
     * @param params
     *        Smart record params.
     * @throws IllegalArgumentException
     * @throws InternalException
     */
    public void createSmartRecord(SmartCreateParams params)
            throws IllegalArgumentException, InternalException {
        ServiceDescriptor descriptor = mDTVManager.getServiceControl()
                .getServiceDescriptor(mCurrentListIndex,
                        params.getServiceIndex());
        mDTVManager.getPvrControl().createSmartRecord(
                getActiveRecordRouteByServiceType(descriptor.getSourceType()),
                params);
    }

    /**
     * Creates timer PVR record.
     * 
     * @param params
     *        Smart record params.
     * @throws IllegalArgumentException
     * @throws InternalException
     */
    public void createTimerRecord(TimerCreateParams params)
            throws IllegalArgumentException, InternalException {
        ServiceDescriptor descriptor = mDTVManager.getServiceControl()
                .getServiceDescriptor(mCurrentListIndex,
                        params.getServiceIndex());
        mDTVManager.getPvrControl().createTimerRecord(
                getActiveRecordRouteByServiceType(descriptor.getSourceType()),
                params);
    }

    /**
     * Creates reminder based on EPG event.
     * 
     * @param param
     * @throws IllegalArgumentException
     * @throws InternalException
     */
    public void createReminder(ReminderSmartParam param)
            throws IllegalArgumentException, InternalException {
        mDTVManager.getReminderControl().createSmart(param);
    }

    /**
     * Creates manual timer reminder.
     * 
     * @param param
     * @throws IllegalArgumentException
     * @throws InternalException
     */
    public void createReminderManual(ReminderTimerParam param)
            throws IllegalArgumentException, InternalException {
        mDTVManager.getReminderControl().createTimer(param);
    }

    /**
     * Return Loaded EPG Events holder
     * 
     * @return Populated events holder
     */
    public TimeEvent[] getLoadedEpgEvents() {
        return mTimeEventHolders;
    }

    /**
     * Set DVB Status Instance.
     */
    public void setDVBStatus(DVBStatus dvbStatus) {
        mDVBStatus = dvbStatus;
    }

    /**
     * Set On Load Finished Listener.
     */
    public void setLoadFinishedListener(
            OnLoadFinishedListener loadFinishedListener) {
        mLoadFinishedListener = loadFinishedListener;
    }

    /** Initialize EPG Date */
    public void initializeDate() {
        TimeDate lCurrentTime = mDTVManager.getSetupControl().getTimeDate();
        mLoadFinishedListener.onLoadFinished(lCurrentTime.getDay() + "/"
                + lCurrentTime.getMonth() + "/" + lCurrentTime.getYear() + " "
                + lCurrentTime.getHour() + ":" + lCurrentTime.getMin());
    }

    public EpgEventGenre getActiveGenre() {
        return mGenre;
    }

    public boolean isIpAndSomeOtherTunerType() {
        return ipAndSomeOtherTunerType;
    }

    public int getPlaybackRouteIDMain() {
        return mPlaybackRouteIDMain;
    }

    public PvrManager getPvrManager() {
        return mPvrManager;
    }

    public ReminderManager getReminderManager() {
        return mReminderManager;
    }
}
