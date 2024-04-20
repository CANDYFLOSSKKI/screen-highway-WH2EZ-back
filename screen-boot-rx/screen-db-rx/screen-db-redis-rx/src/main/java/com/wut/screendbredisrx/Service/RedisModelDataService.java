package com.wut.screendbredisrx.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wut.screencommonrx.Model.CarPlateModel;
import com.wut.screencommonrx.Model.VehicleModel;
import com.wut.screencommonrx.Util.CollectionEmptyUtil;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static com.wut.screencommonrx.Static.DbModuleStatic.*;

@Component
public class RedisModelDataService {
    private final StringRedisTemplate stringRedisTemplate;
    @Qualifier("redisTaskAsyncPool")
    private final Executor redisTaskAsyncPool;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public RedisModelDataService(StringRedisTemplate stringRedisTemplate, Executor redisTaskAsyncPool) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.redisTaskAsyncPool = redisTaskAsyncPool;
    }

    @PostConstruct
    public void resetModelData() {
        stringRedisTemplate.delete(REDIS_KEY_PLATE_MODEL_DATA);
        stringRedisTemplate.delete(REDIS_KEY_FIBER_MODEL_DATA);
        stringRedisTemplate.delete(REDIS_KEY_LASER_MODEL_DATA);
        stringRedisTemplate.delete(REDIS_KEY_WAVE_MODEL_DATA);
    }

    public Boolean isPlateModelDataEmpty() {
        return Boolean.FALSE.equals(stringRedisTemplate.hasKey(REDIS_KEY_PLATE_MODEL_DATA)) || Objects.requireNonNull(stringRedisTemplate.opsForZSet().size(REDIS_KEY_PLATE_MODEL_DATA)) == 0;
    }

    public Boolean isFiberModelDataEmpty() {
        return Boolean.FALSE.equals(stringRedisTemplate.hasKey(REDIS_KEY_FIBER_MODEL_DATA)) || Objects.requireNonNull(stringRedisTemplate.opsForZSet().size(REDIS_KEY_FIBER_MODEL_DATA)) == 0;
    }

    public Boolean isLaserModelDataEmpty() {
        return Boolean.FALSE.equals(stringRedisTemplate.hasKey(REDIS_KEY_LASER_MODEL_DATA)) || Objects.requireNonNull(stringRedisTemplate.opsForZSet().size(REDIS_KEY_LASER_MODEL_DATA)) == 0;
    }

    public Boolean isWaveModelDataEmpty() {
        return Boolean.FALSE.equals(stringRedisTemplate.hasKey(REDIS_KEY_WAVE_MODEL_DATA)) || Objects.requireNonNull(stringRedisTemplate.opsForZSet().size(REDIS_KEY_WAVE_MODEL_DATA)) == 0;
    }

    public CompletableFuture<Void> storeWaveModelData(VehicleModel model) {
        return CompletableFuture.runAsync(() -> {
            try { stringRedisTemplate.opsForZSet().add(REDIS_KEY_WAVE_MODEL_DATA, objectMapper.writeValueAsString(model), model.getTimestamp().doubleValue()); }
            catch (JsonProcessingException ignored) {}
        },redisTaskAsyncPool);
    }

    public CompletableFuture<Void> storeFiberModelData(VehicleModel model) {
        return CompletableFuture.runAsync(() -> {
            try { stringRedisTemplate.opsForZSet().add(REDIS_KEY_FIBER_MODEL_DATA, objectMapper.writeValueAsString(model), model.getTimestamp().doubleValue()); }
            catch (JsonProcessingException ignored) {}
        },redisTaskAsyncPool);
    }

    public CompletableFuture<Void> storeLaserModelData(VehicleModel model) {
        return CompletableFuture.runAsync(() -> {
            try { stringRedisTemplate.opsForZSet().add(REDIS_KEY_LASER_MODEL_DATA, objectMapper.writeValueAsString(model), model.getTimestamp().doubleValue()); }
            catch (JsonProcessingException ignored) {}
        },redisTaskAsyncPool);
    }

    public CompletableFuture<Void> storePlateModelData(CarPlateModel model) {
        return CompletableFuture.runAsync(() -> {
            try { stringRedisTemplate.opsForZSet().add(REDIS_KEY_PLATE_MODEL_DATA, objectMapper.writeValueAsString(model), model.getTimestamp().doubleValue()); }
            catch (JsonProcessingException ignored) {}
        },redisTaskAsyncPool);
    }

    public CompletableFuture<List<VehicleModel>> collectFiberModelData(double score) {
        return CompletableFuture.supplyAsync(() -> {
           if (isFiberModelDataEmpty()) { return null; }
           Set<String> fiberModelStrSet = stringRedisTemplate.opsForZSet().rangeByScore(REDIS_KEY_FIBER_MODEL_DATA, score, score);
           if (CollectionEmptyUtil.forSet(fiberModelStrSet)) { return null; }
           return Objects.requireNonNull(fiberModelStrSet).stream().map(i -> {
               try { return objectMapper.readValue(i, VehicleModel.class); }
               catch (JsonProcessingException e) { return null; }
           }).filter(Objects::nonNull).toList();
        },redisTaskAsyncPool);
    }

    public CompletableFuture<List<VehicleModel>> collectLaserModelData(double score) {
        return CompletableFuture.supplyAsync(() -> {
            if (isLaserModelDataEmpty()) { return null; }
            Set<String> laserModelStrSet = stringRedisTemplate.opsForZSet().rangeByScore(REDIS_KEY_LASER_MODEL_DATA, score, score);
            if (CollectionEmptyUtil.forSet(laserModelStrSet)) { return null; }
            return Objects.requireNonNull(laserModelStrSet).stream().map(i -> {
                try { return objectMapper.readValue(i, VehicleModel.class); }
                catch (JsonProcessingException e) { return null; }
            }).filter(Objects::nonNull).toList();
        },redisTaskAsyncPool);
    }

    public CompletableFuture<List<VehicleModel>> collectWaveModelData(double score) {
        return CompletableFuture.supplyAsync(() -> {
           if (isWaveModelDataEmpty()) { return null; }
           Set<String> waveModelStrSet = stringRedisTemplate.opsForZSet().rangeByScore(REDIS_KEY_WAVE_MODEL_DATA, score, score);
           if (CollectionEmptyUtil.forSet(waveModelStrSet)) { return null; }
           return Objects.requireNonNull(waveModelStrSet).stream().map(i -> {
               try { return objectMapper.readValue(i, VehicleModel.class); }
               catch (JsonProcessingException e) { return null; }
           }).filter(Objects::nonNull).toList();
        },redisTaskAsyncPool);
    }

    public CompletableFuture<List<CarPlateModel>> collectPlateModelData(double min, double max) {
        return CompletableFuture.supplyAsync(() -> {
            if (isPlateModelDataEmpty()) { return null; }
            Set<String> plateModelStrSet = stringRedisTemplate.opsForZSet().rangeByScore(REDIS_KEY_PLATE_MODEL_DATA, min, max);
            if (CollectionEmptyUtil.forSet(plateModelStrSet)) { return null; }
            return Objects.requireNonNull(plateModelStrSet).stream().map(i -> {
                try { return objectMapper.readValue(i, CarPlateModel.class); }
                catch (JsonProcessingException e) { return null; }
            }).filter(Objects::nonNull).toList();
        },redisTaskAsyncPool);
    }

    public void removePlateModelData(List<CarPlateModel> list) {
        List<String> plateReadyToMoveStrList = list.stream().map(i -> {
            try { return objectMapper.writeValueAsString(i); }
            catch (JsonProcessingException e) { return null; }
        }).filter(Objects::nonNull).toList();
        plateReadyToMoveStrList.stream().forEach(plateStr -> {
            stringRedisTemplate.opsForZSet().remove(REDIS_KEY_PLATE_MODEL_DATA, plateStr);
        });
    }

}
