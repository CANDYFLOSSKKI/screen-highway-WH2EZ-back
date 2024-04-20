package com.wut.screenwebsx.Service;

import com.wut.screencommonsx.Request.EventTrackReq;
import com.wut.screencommonsx.Response.Event.EventDataResp;
import com.wut.screencommonsx.Response.Event.EventInfoData;
import com.wut.screencommonsx.Response.Event.EventStatisticData;
import com.wut.screencommonsx.Response.PositionRecordData;
import com.wut.screencommonsx.Response.Track.EventTrackDataResp;
import com.wut.screencommonsx.Response.Track.EventTrackInfoData;
import com.wut.screencommonsx.Util.CollectionEmptyUtil;
import com.wut.screencommonsx.Util.DataParamParseUtil;
import com.wut.screencommonsx.Util.DateParamParseUtil;
import com.wut.screencommonsx.Util.ModelTransformUtil;
import com.wut.screendbmysqlsx.Model.CarEvent;
import com.wut.screendbmysqlsx.Model.SecInfo;
import com.wut.screendbmysqlsx.Model.Traj;
import com.wut.screendbmysqlsx.Service.CarEventService;
import com.wut.screendbmysqlsx.Service.TrajService;
import com.wut.screendbmysqlsx.Util.DbModelTransformUtil;
import com.wut.screenwebsx.Config.DockingInterfaceConfig.Docking;
import com.wut.screenwebsx.Context.SecInfoDataContext;
import com.wut.screenwebsx.Model.EventTrackModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import static com.wut.screencommonsx.Static.WebModuleStatic.*;

@Component
public class EventWebService {
    @Qualifier("webTaskAsyncPool")
    private final Executor webTaskAsyncPool;
    private final CarEventService carEventService;
    private final TrajService trajService;
    private final SecInfoDataContext secInfoDataContext;

    @Autowired
    public EventWebService(Executor webTaskAsyncPool, CarEventService carEventService, TrajService trajService, SecInfoDataContext secInfoDataContext) {
        this.webTaskAsyncPool = webTaskAsyncPool;
        this.carEventService = carEventService;
        this.trajService = trajService;
        this.secInfoDataContext = secInfoDataContext;
    }

    @Docking
    public EventDataResp getEventData(long timestamp){
        String tableDateStr = DateParamParseUtil.getDateTableStr(timestamp);
        List<CarEvent> carEventList = new ArrayList<>();
        try {
            List<CarEvent> data = CompletableFuture.supplyAsync(() -> {
                return carEventService.getListByDate(tableDateStr);
            }, webTaskAsyncPool).get(ASYNC_SERVICE_TIMEOUT, TimeUnit.SECONDS);
            if (!CollectionEmptyUtil.forList(data)) {
                carEventList.addAll(data);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return collectEventData(carEventList);
    }

    @Docking
    public EventTrackDataResp getEventTrackData(EventTrackReq req) {
        String tableDateStr = DateParamParseUtil.getDateTableStr(req.getTimestamp());
        EventTrackModel eventTrackModel = initEventTrackModel(tableDateStr, req.getUuid());
        return collectEventTrackData(eventTrackModel);
    }

    public EventDataResp collectEventData(List<CarEvent> carEventList) {
        List<PositionRecordData> positionRecordList = initPositionRecordCollection();
        List<EventInfoData> infoDataList = new ArrayList<>();
        EventStatisticData statisticData = ModelTransformUtil.getEventStatisticInstance();
        if (CollectionEmptyUtil.forList(carEventList)) { return new EventDataResp(statisticData, positionRecordList, infoDataList); }
        carEventList.stream().forEach(carEvent -> {
            recordPosition(positionRecordList, carEvent);
            recordStatistic(statisticData, carEvent);
            if (carEvent.getStatus() != EVENT_STATUS_FINISHED) {
                infoDataList.add(DbModelTransformUtil.eventToInfoData(carEvent));
            }
        });
        return new EventDataResp(statisticData, positionRecordList, infoDataList);
    }

    public EventTrackDataResp collectEventTrackData(EventTrackModel eventTrackModel) {
        CarEvent event = eventTrackModel.getEvent();
        List<Traj> trajList = eventTrackModel.getTrajList();
        List<EventTrackInfoData> trackInfoList = new ArrayList<>();
        if (event == null || CollectionEmptyUtil.forList(trajList)) { return new EventTrackDataResp(null, trackInfoList); }
        EventInfoData eventInfo = DbModelTransformUtil.eventToInfoData(event);
        EventTrackInfoData eventTrackInfo = DbModelTransformUtil.trajToTrackInfoData(trajList.stream().filter(traj -> Objects.equals(traj.getTimestamp(), event.getEndTimestamp())).findAny().get());
        eventTrackInfo.getFrameList().addAll(trajList.stream().map(DbModelTransformUtil::trajToFrameData).toList());
        trackInfoList.add(eventTrackInfo);
        return new EventTrackDataResp(eventInfo, trackInfoList);
    }

    public List<PositionRecordData> initPositionRecordCollection() {
        List<SecInfo> secInfoList = secInfoDataContext.getSecInfoList();
        return secInfoList.stream().map(DbModelTransformUtil::secInfoToPositionRecordData).toList();
    }

    public void recordPosition(List<PositionRecordData> positionRecordList, CarEvent carEvent) {
        positionRecordList.stream().filter(record -> {
            return DataParamParseUtil.isBetweenRamp(Double.parseDouble(carEvent.getStartMileage()), record.getXsecValue(), record.getXsecValue());
        }).findFirst().ifPresent(record -> {
            switch (carEvent.getDirection()) {
                case TRAJ_ROAD_DIRECT_TO_EZ -> record.setValueToEZ(record.getValueToEZ() + 1);
                case TRAJ_ROAD_DIRECT_TO_WH -> record.setValueToWH(record.getValueToWH() + 1);
            }
        });
    }

    public void recordStatistic(EventStatisticData eventStatisticData, CarEvent carEvent) {
        eventStatisticData.setTotal(eventStatisticData.getTotal() + 1);
        if (carEvent.getStatus() == EVENT_STATUS_PENDING) {
            eventStatisticData.setPending(eventStatisticData.getPending() + 1);
        }
        switch (carEvent.getEventType()) {
            case EVENT_TYPE_PARKING -> eventStatisticData.setParking(eventStatisticData.getParking() + 1);
            case EVENT_TYPE_AGAINST -> eventStatisticData.setAgainst(eventStatisticData.getAgainst() + 1);
            case EVENT_TYPE_FAST -> eventStatisticData.setFast(eventStatisticData.getFast() + 1);
            case EVENT_TYPE_SLOW -> eventStatisticData.setSlow(eventStatisticData.getSlow() + 1);
            case EVENT_TYPE_OCCUPY -> eventStatisticData.setOccupy(eventStatisticData.getOccupy() + 1);
        }
    }

    public EventTrackModel initEventTrackModel(String date, long uuid){
        CarEvent carEvent = null;
        List<Traj> trajList = new ArrayList<>();
        try {
            carEvent = CompletableFuture.supplyAsync(() -> {
                return carEventService.getOneByUuid(date, uuid);
            }, webTaskAsyncPool).get(ASYNC_SERVICE_TIMEOUT, TimeUnit.SECONDS);
            Optional.ofNullable(carEvent).ifPresent(event -> trajList.addAll(initEventTrackTrajList(date, event)));
        } catch (Exception e) { e.printStackTrace(); }
        return new EventTrackModel(carEvent, trajList);
    }

    public List<Traj> initEventTrackTrajList(String date, CarEvent event) {
        List<Traj> trajList = new ArrayList<>();
        try {
            trajList = CompletableFuture.supplyAsync(() -> {
                return trajService.getListByEventInterval(date, event.getStartTimestamp(), event.getEndTimestamp());
            }, webTaskAsyncPool).get(ASYNC_SERVICE_TIMEOUT, TimeUnit.SECONDS);
        } catch (Exception e) { e.printStackTrace(); }
        return CollectionEmptyUtil.forList(trajList) ? new ArrayList<>() : trajList;
    }

}
