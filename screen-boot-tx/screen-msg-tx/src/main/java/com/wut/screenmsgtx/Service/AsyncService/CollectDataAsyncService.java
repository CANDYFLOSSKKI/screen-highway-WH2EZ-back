package com.wut.screenmsgtx.Service.AsyncService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wut.screencommontx.Entity.CollectDataParam;
import com.wut.screencommontx.Util.CollectionEmptyUtil;
import com.wut.screendbtx.Model.Fiber;
import com.wut.screendbtx.Model.Laser;
import com.wut.screendbtx.Model.Plate;
import com.wut.screendbtx.Model.Wave;
import com.wut.screendbtx.Service.FiberService;
import com.wut.screendbtx.Service.LaserService;
import com.wut.screendbtx.Service.PlateService;
import com.wut.screendbtx.Service.WaveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static com.wut.screencommontx.Static.MsgModuleStatic.*;

@Component
public class CollectDataAsyncService {
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final PlateService plateService;
    private final FiberService fiberService;
    private final LaserService laserService;
    private final WaveService waveService;
    @Qualifier("msgTaskAsyncPool")
    private final Executor msgTaskAsyncPool;

    @Autowired
    public CollectDataAsyncService(StringRedisTemplate stringRedisTemplate, PlateService plateService, FiberService fiberService, LaserService laserService, WaveService waveService, Executor msgTaskAsyncPool) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.plateService = plateService;
        this.fiberService = fiberService;
        this.laserService = laserService;
        this.waveService = waveService;
        this.msgTaskAsyncPool = msgTaskAsyncPool;
    }

    public CompletableFuture<Plate> collectPlateMinTimestamp(CollectDataParam param) {
        return CompletableFuture.supplyAsync(() -> plateService.collectMinTimestamp(param),msgTaskAsyncPool);
    }

    public CompletableFuture<Fiber> collectFiberMinTimestamp(CollectDataParam param) {
        return CompletableFuture.supplyAsync(() -> fiberService.collectMinTimestamp(param),msgTaskAsyncPool);
    }

    public CompletableFuture<Laser> collectLaserMinTimestamp(CollectDataParam param) {
        return CompletableFuture.supplyAsync(() -> laserService.collectMinTimestamp(param),msgTaskAsyncPool);
    }

    public CompletableFuture<Wave> collectWaveMinTimestamp(CollectDataParam param) {
        return CompletableFuture.supplyAsync(() -> waveService.collectMinTimestamp(param),msgTaskAsyncPool);
    }

    public CompletableFuture<List<Plate>> collectPlateFromTime(CollectDataParam param) {
        return CompletableFuture.supplyAsync(() -> plateService.collectFromTime(param),msgTaskAsyncPool);
    }

    public CompletableFuture<List<Fiber>> collectFiberFromTime(CollectDataParam param) {
        return CompletableFuture.supplyAsync(() -> fiberService.collectFromTime(param),msgTaskAsyncPool);
    }

    public CompletableFuture<List<Laser>> collectLaserFromTime(CollectDataParam param) {
        return CompletableFuture.supplyAsync(() -> laserService.collectFromTime(param),msgTaskAsyncPool);
    }

    public CompletableFuture<List<Wave>> collectWaveFromTime(CollectDataParam param) {
        return CompletableFuture.supplyAsync(() -> waveService.collectFromTime(param),msgTaskAsyncPool);
    }

    public CompletableFuture<Boolean> storeCollectPlateData(List<Plate> plateData) {
        return CompletableFuture.supplyAsync(() -> {
            if (CollectionEmptyUtil.forList(plateData)) { return false; }
            stringRedisTemplate.opsForList().rightPushAll(REDIS_KEY_PLATE_DATA, plateData.stream().map(i -> {
                try { return objectMapper.writeValueAsString(i); }
                catch (JsonProcessingException e) { return null; }
            }).filter(Objects::nonNull).toList());
            return true;
        },msgTaskAsyncPool);
    }

    public CompletableFuture<Boolean> storeCollectFiberData(List<Fiber> fiberData) {
        return CompletableFuture.supplyAsync(() -> {
            if (CollectionEmptyUtil.forList(fiberData)) { return false; }
            stringRedisTemplate.opsForList().rightPushAll(REDIS_KEY_FIBER_DATA, fiberData.stream().map(i -> {
                try { return objectMapper.writeValueAsString(i); }
                catch (JsonProcessingException e) { return null; }
            }).filter(Objects::nonNull).toList());
            return true;
        },msgTaskAsyncPool);
    }

    public CompletableFuture<Boolean> storeCollectLaserData(List<Laser> laserData) {
        return CompletableFuture.supplyAsync(() -> {
            if (CollectionEmptyUtil.forList(laserData)) { return false; }
            stringRedisTemplate.opsForList().rightPushAll(REDIS_KEY_LASER_DATA, laserData.stream().map(i -> {
                try { return objectMapper.writeValueAsString(i); }
                catch (JsonProcessingException e) { return null; }
            }).filter(Objects::nonNull).toList());
            return true;
        },msgTaskAsyncPool);
    }

    public CompletableFuture<Boolean> storeCollectWaveData(List<Wave> waveData) {
        return CompletableFuture.supplyAsync(() -> {
            if (CollectionEmptyUtil.forList(waveData)) { return false; }
            stringRedisTemplate.opsForList().rightPushAll(REDIS_KEY_WAVE_DATA, waveData.stream().map(i -> {
                try { return objectMapper.writeValueAsString(i); }
                catch (JsonProcessingException e) { return null; }
            }).filter(Objects::nonNull).toList());
            return true;
        },msgTaskAsyncPool);
    }

}
