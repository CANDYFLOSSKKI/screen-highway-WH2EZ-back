package com.wut.screendbredisrx.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import static com.wut.screencommonrx.Static.DbModuleStatic.REDIS_KEY_VEHICLE_FLUSH_MODEL;

@Component
public class RedisModelFlushService {
    private final StringRedisTemplate stringRedisTemplate;
    @Qualifier("redisTaskAsyncPool")
    private final Executor redisTaskAsyncPool;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public RedisModelFlushService(StringRedisTemplate stringRedisTemplate, Executor redisTaskAsyncPool) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.redisTaskAsyncPool = redisTaskAsyncPool;
    }

    @PostConstruct
    public void resetModelFlush() {
        stringRedisTemplate.delete(REDIS_KEY_VEHICLE_FLUSH_MODEL);
    }

    public Boolean isVehicleFlushModelEmpty() {
        return Boolean.FALSE.equals(stringRedisTemplate.hasKey(REDIS_KEY_VEHICLE_FLUSH_MODEL)) || Objects.requireNonNull(stringRedisTemplate.opsForZSet().size(REDIS_KEY_VEHICLE_FLUSH_MODEL)) == 0;
    }

    public CompletableFuture<Boolean> storeFlushVehicleModel(List<VehicleModel> list) {
        return CompletableFuture.supplyAsync(() -> {
            if (CollectionEmptyUtil.forList(list)) { return false; }
            list.stream().forEach(model -> {
                try { stringRedisTemplate.opsForZSet().add(REDIS_KEY_VEHICLE_FLUSH_MODEL, objectMapper.writeValueAsString(model), model.getTimestamp()); }
                catch (JsonProcessingException ignored) {}
            });
           return true;
        },redisTaskAsyncPool);
    }

    public CompletableFuture<List<VehicleModel>> collectFlushVehicleModel(double score) {
        return CompletableFuture.supplyAsync(() -> {
           if (isVehicleFlushModelEmpty()) { return List.of(); }
           Set<String> flushModelStrSet = stringRedisTemplate.opsForZSet().rangeByScore(REDIS_KEY_VEHICLE_FLUSH_MODEL, score, score);
           if (CollectionEmptyUtil.forSet(flushModelStrSet)) { return null; }
           return Objects.requireNonNull(flushModelStrSet).stream().map(i -> {
               try { return objectMapper.readValue(i, VehicleModel.class); }
               catch (JsonProcessingException e) { return null; }
           }).filter(Objects::nonNull).toList();
        },redisTaskAsyncPool);
    }

}
