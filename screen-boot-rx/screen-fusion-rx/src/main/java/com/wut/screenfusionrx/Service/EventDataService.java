package com.wut.screenfusionrx.Service;

import com.wut.screencommonrx.Entity.EventTypeData;
import com.wut.screencommonrx.Model.CarEventModel;
import com.wut.screencommonrx.Util.CollectionEmptyUtil;
import com.wut.screencommonrx.Util.MessagePrintUtil;
import com.wut.screencommonrx.Util.ModelTransformUtil;
import com.wut.screendbmysqlrx.Model.CarEvent;
import com.wut.screendbmysqlrx.Model.Traj;
import com.wut.screendbmysqlrx.Util.DbModelTransformUtil;
import com.wut.screendbredisrx.Service.RedisBatchCacheService;
import com.wut.screendbredisrx.Service.RedisEventDataService;
import com.wut.screendbredisrx.Service.RedisTrajFusionService;
import com.wut.screenfusionrx.Util.EventEstimateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import static com.wut.screencommonrx.Static.FusionModuleStatic.*;

@Component
public class EventDataService {
    private final RedisTrajFusionService redisTrajFusionService;
    private final RedisEventDataService redisEventDataService;
    private final RedisBatchCacheService redisBatchCacheService;
    @Qualifier("fusionTaskAsyncPool")
    private final Executor fusionTaskAsyncPool;


    @Autowired
    public EventDataService(RedisTrajFusionService redisTrajFusionService, RedisEventDataService redisEventDataService, Executor fusionTaskAsyncPool, RedisBatchCacheService redisBatchCacheService) {
        this.redisTrajFusionService = redisTrajFusionService;
        this.redisEventDataService = redisEventDataService;
        this.fusionTaskAsyncPool = fusionTaskAsyncPool;
        this.redisBatchCacheService = redisBatchCacheService;
    }

    public Map<String, CarEventModel> collectEventModelData(long timestamp) throws Exception{
        var trajDataTask = redisTrajFusionService.collectTrajData(timestamp);
        var eventModelDataTask = redisEventDataService.collectEventModelData();
        CompletableFuture.allOf(trajDataTask, eventModelDataTask).get(ASYNC_SERVICE_TIMEOUT, TimeUnit.SECONDS);
        Map<String, CarEventModel> eventModelMap = new HashMap<>();
        if (!CollectionEmptyUtil.forMap(eventModelDataTask.get())) {
            eventModelMap.putAll(eventModelDataTask.get());
        }
        if (!CollectionEmptyUtil.forList(trajDataTask.get())) {
            List<Traj> trajList = new ArrayList<>(trajDataTask.get());
            trajList.forEach(traj -> matchEventModel(traj, eventModelMap));
        }
        if (!CollectionEmptyUtil.forMap(eventModelMap)) {
            filterEventModel(eventModelMap, timestamp);
            saveEventModel(eventModelMap, timestamp);
        }
        return eventModelMap;
    }

    public void matchEventModel(Traj traj, Map<String, CarEventModel> eventModelMap) {
        CarEventModel carEventModel = eventModelMap.get(Long.toString(traj.getTrajId()));
        double firstSpeed = 0.0, lastSpeed = 0.0;
        if (carEventModel != null) {
            firstSpeed = carEventModel.getSpeedList().get(0);
            lastSpeed = carEventModel.getSpeedList().get(carEventModel.getSpeedList().size() - 1);
            double lastFrenetX = carEventModel.getEndFrenetX();
            // 逆行类型事件判断(逆行至少需要两个轨迹帧数据才能确定)
            if (EventEstimateUtil.hasAgainstEvent(traj, lastFrenetX)) {
                updateEventModelMap(traj, eventModelMap, EVENT_TYPE_AGAINST);
            }
            updateEventModelItem(traj, carEventModel);
        } else {
            eventModelMap.put(Long.toString(traj.getTrajId()), DbModelTransformUtil.trajToCarEventModel(traj, EVENT_TYPE_NORMAL.value));
            carEventModel = eventModelMap.get(Long.toString(traj.getTrajId()));
        }
        updateSpeedList(traj, carEventModel);
        // 违停类型事件判断
        if (EventEstimateUtil.hasParkingEvent(traj, firstSpeed, lastSpeed)) {
            updateEventModelMap(traj, eventModelMap, EVENT_TYPE_PARKING);
        }
        // 占用应急车道类型事件判断
        if (EventEstimateUtil.hasOccupyEvent(traj)) {
            updateEventModelMap(traj, eventModelMap, EVENT_TYPE_OCCUPY);
        }
        // 超速类型事件判断
        if (EventEstimateUtil.hasFastEvent(traj, lastSpeed)) {
            updateEventModelMap(traj, eventModelMap, EVENT_TYPE_FAST);
        }
        // 慢速类型事件判断
        if (EventEstimateUtil.hasSlowEvent(traj, lastSpeed)) {
            updateEventModelMap(traj, eventModelMap, EVENT_TYPE_SLOW);
        }
    }

    public void updateSpeedList(Traj traj, CarEventModel carEventModel) {
        List<Double> speedList = carEventModel.getSpeedList();
        speedList.add(traj.getSpeedX());
        if (speedList.size() == 5) {
            speedList.remove(0);
        }
    }

    public void updateEventModelMap(Traj traj, Map<String, CarEventModel> eventModelMap, EventTypeData eventTypeData) {
        CarEventModel carEventModel = eventModelMap.get(traj.getTrajId() + eventTypeData.suffix);
        Optional.ofNullable(carEventModel).ifPresentOrElse(
            (eventModel) -> updateEventModelItem(traj, eventModel),
            () -> eventModelMap.put(traj.getTrajId() + eventTypeData.suffix, DbModelTransformUtil.trajToCarEventModel(traj, eventTypeData.value))
        );
    }

    public void updateEventModelItem(Traj traj, CarEventModel carEventModel) {
        carEventModel.setEventTime(carEventModel.getEventTime() + 1);
        carEventModel.setPicLicense(traj.getCarId());
        carEventModel.setEndTimestamp(traj.getTimestamp());
        carEventModel.setEndFrenetX(traj.getFrenetX());
        carEventModel.setRoadDirect(traj.getRoadDirect());
        carEventModel.setChanged(true);
    }

    public void filterEventModel(Map<String, CarEventModel> carEventModelMap, long timestamp) {
        List<String> readyToMoveList = carEventModelMap.entrySet().stream().filter(entry -> {
            CarEventModel carEventModel = entry.getValue();
            EventTypeData eventTypeData = ModelTransformUtil.getEventTypeInstance(carEventModel.getEventType());
            if (carEventModel.isChanged()) { return false; }
            return timestamp - carEventModel.getEndTimestamp() >= eventTypeData.getTimeout();
        }).map(Map.Entry::getKey).toList();
        if (!CollectionEmptyUtil.forList(readyToMoveList)) {
            for (String key : readyToMoveList) {
                carEventModelMap.remove(key);
            }
        }
    }

    public void saveEventModel(Map<String, CarEventModel> carEventModelMap, long timestamp) throws Exception{
        if (CollectionEmptyUtil.forMap(carEventModelMap)) { return; }
        List<CarEvent> readyToInsertList = new ArrayList<>();
        List<CarEvent> readyToUpdateList = new ArrayList<>();
        carEventModelMap.entrySet().stream().filter(entry -> entry.getValue().isChanged()).forEach(entry -> {
            CarEventModel carEventModel = entry.getValue();
            carEventModel.setChanged(false);
            if (carEventModel.getEventType() == EVENT_TYPE_NORMAL.value) { return; }
            if (carEventModel.isStored()) {
                readyToUpdateList.add(DbModelTransformUtil.eventModelToEvent(carEventModel));
            } else if (carEventModel.getEventTime() > ModelTransformUtil.getEventTypeInstance(carEventModel.getEventType()).time){
                carEventModel.setStored(true);
                readyToInsertList.add(DbModelTransformUtil.eventModelToEvent(carEventModel));
            }
        });
        redisBatchCacheService.storeEventCache(readyToInsertList, readyToUpdateList, timestamp);
    }

    public void storeEventModelData(Map<String, CarEventModel> carEventModelMap, long timestamp) throws Exception{
        redisEventDataService.storeEventModelData(carEventModelMap).get(ASYNC_SERVICE_TIMEOUT, TimeUnit.SECONDS);
        MessagePrintUtil.printEventData(timestamp);
    }

}
