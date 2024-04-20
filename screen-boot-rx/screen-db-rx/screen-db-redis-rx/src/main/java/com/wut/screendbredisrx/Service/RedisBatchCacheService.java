package com.wut.screendbredisrx.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wut.screencommonrx.Util.CollectionEmptyUtil;
import com.wut.screencommonrx.Util.DateParamParseUtil;
import com.wut.screencommonrx.Util.MessagePrintUtil;
import com.wut.screendbmysqlrx.Model.CarEvent;
import com.wut.screendbmysqlrx.Model.Traj;
import com.wut.screendbmysqlrx.Service.CarEventService;
import com.wut.screendbmysqlrx.Service.TrajService;
import com.wut.screendbredisrx.Context.BatchCacheContext;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static com.wut.screencommonrx.Static.DbModuleStatic.*;
import static com.wut.screencommonrx.Static.FusionModuleStatic.ASYNC_SERVICE_TIMEOUT;

@Component
public class RedisBatchCacheService {
    private final BatchCacheContext batchCacheContext;
    private final StringRedisTemplate stringRedisTemplate;
    private final TrajService trajService;
    private final CarEventService carEventService;
    @Qualifier("redisTaskAsyncPool")
    private final Executor redisTaskAsyncPool;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final ReentrantLock TRAJ_CACHE_LOCK = new ReentrantLock();
    private static final ReentrantLock EVENT_CACHE_LOCK = new ReentrantLock();

    @Autowired
    public RedisBatchCacheService(BatchCacheContext batchCacheContext, StringRedisTemplate stringRedisTemplate, TrajService trajService, CarEventService carEventService, Executor redisTaskAsyncPool) {
        this.batchCacheContext = batchCacheContext;
        this.stringRedisTemplate = stringRedisTemplate;
        this.trajService = trajService;
        this.carEventService = carEventService;
        this.redisTaskAsyncPool = redisTaskAsyncPool;
    }

    @PostConstruct
    public void resetBatchCache() {
        stringRedisTemplate.delete(REDIS_KEY_TRAJ_BATCH_CACHE);
        stringRedisTemplate.delete(REDIS_KEY_EVENT_INSERT_BATCH_CACHE);
        stringRedisTemplate.delete(REDIS_KEY_EVENT_UPDATE_BATCH_CACHE);
    }

    public Boolean isTrajBatchCacheEmpty() {
        return Boolean.FALSE.equals(stringRedisTemplate.hasKey(REDIS_KEY_TRAJ_BATCH_CACHE)) || Objects.requireNonNull(stringRedisTemplate.opsForList().size(REDIS_KEY_TRAJ_BATCH_CACHE)) == 0;
    }

    public Boolean isEventInsertBatchCacheEmpty() {
        return Boolean.FALSE.equals(stringRedisTemplate.hasKey(REDIS_KEY_EVENT_INSERT_BATCH_CACHE)) || Objects.requireNonNull(stringRedisTemplate.opsForList().size(REDIS_KEY_EVENT_INSERT_BATCH_CACHE)) == 0;
    }

    public Boolean isEventUpdateBatchCacheEmpty() {
        return Boolean.FALSE.equals(stringRedisTemplate.hasKey(REDIS_KEY_EVENT_UPDATE_BATCH_CACHE)) || Objects.requireNonNull(stringRedisTemplate.opsForList().size(REDIS_KEY_EVENT_UPDATE_BATCH_CACHE)) == 0;
    }

    public void storeTrajCache(List<Traj> trajList, long timestamp){
        if (CollectionEmptyUtil.forList(trajList)) { return; }
        stringRedisTemplate.opsForList().rightPushAll(REDIS_KEY_TRAJ_BATCH_CACHE, trajList.stream().map(traj -> {
            try { return objectMapper.writeValueAsString(traj); }
            catch (JsonProcessingException e) { return null; }
        }).filter(Objects::nonNull).toList());
        if (batchCacheContext.recordTrajCacheTimestamp(timestamp)) {
            List<Traj> readyToInsertList = getTrajBatchCache();
            saveTrajBatchCache(DateParamParseUtil.getDateTimeStr(timestamp), readyToInsertList);
        }
    }

    public void storeEventCache(List<CarEvent> insertList, List<CarEvent> updateList, long timestamp) throws Exception{
        var storeEventInsertTask = storeEventInsertCache(insertList);
        var storeEventUpdateTask = storeEventUpdateCache(updateList);
        CompletableFuture.allOf(storeEventInsertTask, storeEventUpdateTask).get(ASYNC_SERVICE_TIMEOUT, TimeUnit.SECONDS);
        if (batchCacheContext.recordEventCacheTimestamp(timestamp)) {
            List<CarEvent> readyToInsertList = getEventInsertBatchCache();
            List<CarEvent> readyToUpdateList = getEventUpdateBatchCache();
            saveEventBatchCache(DateParamParseUtil.getDateTimeStr(timestamp), readyToInsertList, readyToUpdateList);
        }
    }

    public CompletableFuture<Void> storeEventInsertCache(List<CarEvent> carEventList) {
        return CompletableFuture.runAsync(() -> {
            if (CollectionEmptyUtil.forList(carEventList)) { return; }
            stringRedisTemplate.opsForList().rightPushAll(REDIS_KEY_EVENT_INSERT_BATCH_CACHE, carEventList.stream().map(event -> {
                try { return objectMapper.writeValueAsString(event); }
                catch (JsonProcessingException e) { return null; }
            }).filter(Objects::nonNull).toList());
        }, redisTaskAsyncPool);
    }

    public CompletableFuture<Void> storeEventUpdateCache(List<CarEvent> carEventList) {
        return CompletableFuture.runAsync(() -> {
            if (CollectionEmptyUtil.forList(carEventList)) { return; }
            stringRedisTemplate.opsForList().rightPushAll(REDIS_KEY_EVENT_UPDATE_BATCH_CACHE, carEventList.stream().map(event -> {
                try { return objectMapper.writeValueAsString(event); }
                catch (JsonProcessingException e) { return null; }
            }).filter(Objects::nonNull).toList());
        }, redisTaskAsyncPool);
    }

    public List<Traj> getTrajBatchCache(){
        if (isTrajBatchCacheEmpty()) { return null; }
        return Objects.requireNonNull(stringRedisTemplate.opsForList().leftPop(REDIS_KEY_TRAJ_BATCH_CACHE, Objects.requireNonNull(stringRedisTemplate.opsForList().size(REDIS_KEY_TRAJ_BATCH_CACHE)))).stream().map(str -> {
            try { return objectMapper.readValue(str, Traj.class); }
            catch (JsonProcessingException e) { return null; }
        }).filter(Objects::nonNull).toList();
    }

    public List<CarEvent> getEventInsertBatchCache(){
        if (isEventInsertBatchCacheEmpty()) { return null; }
        return Objects.requireNonNull(stringRedisTemplate.opsForList().leftPop(REDIS_KEY_EVENT_INSERT_BATCH_CACHE, Objects.requireNonNull(stringRedisTemplate.opsForList().size(REDIS_KEY_EVENT_INSERT_BATCH_CACHE)))).stream().map(str -> {
            try { return objectMapper.readValue(str, CarEvent.class); }
            catch (JsonProcessingException e) { return null; }
        }).filter(Objects::nonNull).toList();
    }

    public List<CarEvent> getEventUpdateBatchCache() {
        if (isEventUpdateBatchCacheEmpty()) { return null; }
        return  Objects.requireNonNull(stringRedisTemplate.opsForList().leftPop(REDIS_KEY_EVENT_UPDATE_BATCH_CACHE, Objects.requireNonNull(stringRedisTemplate.opsForList().size(REDIS_KEY_EVENT_UPDATE_BATCH_CACHE)))).stream().map(str -> {
            try { return objectMapper.readValue(str, CarEvent.class); }
            catch (JsonProcessingException e) { return null; }
        }).filter(Objects::nonNull).collect(
            Collectors.groupingBy((event) -> event.getTrajId() + "#" + event.getEventType())
        ).values().stream().map(
            (eventList) -> eventList.stream().max(Comparator.comparingLong(CarEvent::getEndTimestamp)).orElse(null)
        ).filter(Objects::nonNull).toList();
    }

    public void saveTrajBatchCache(String time, List<Traj> readyToInsertList) {
        CompletableFuture.runAsync(() -> {
            try {
                TRAJ_CACHE_LOCK.lock();
                if (CollectionEmptyUtil.forList(readyToInsertList)) { return; }
                trajService.storeTrajData(time, readyToInsertList);
                MessagePrintUtil.printTrajCacheSave();
            } catch (Exception e) { e.printStackTrace(); }
            finally { TRAJ_CACHE_LOCK.unlock(); }
        }, redisTaskAsyncPool);
    }

    public void saveEventBatchCache(String time, List<CarEvent> readyToInsertList, List<CarEvent> readyToUpdateList) {
        CompletableFuture.runAsync(() -> {
            try {
                EVENT_CACHE_LOCK.lock();
                if (!CollectionEmptyUtil.forList(readyToInsertList)) {
                    carEventService.storeEventData(time, readyToInsertList);
                }
                if (!CollectionEmptyUtil.forList(readyToUpdateList)) {
                    // 指定时间段的数据中可能存在对相同事件的多次更新(采用<轨迹号#事件类型>作为唯一键标识)
                    // 筛选出每组更新信息中终止时间戳最大的项即可
                    carEventService.updateEventData(time, readyToUpdateList);
                }
                MessagePrintUtil.printEventCacheSave();
            } catch (Exception e) { e.printStackTrace(); }
            finally { EVENT_CACHE_LOCK.unlock(); }
        }, redisTaskAsyncPool);
    }

}
