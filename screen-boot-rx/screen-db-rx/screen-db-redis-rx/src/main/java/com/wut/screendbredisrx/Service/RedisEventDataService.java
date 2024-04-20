package com.wut.screendbredisrx.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wut.screencommonrx.Model.CarEventModel;
import com.wut.screencommonrx.Util.CollectionEmptyUtil;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.function.SupplierUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static com.wut.screencommonrx.Static.DbModuleStatic.REDIS_KEY_EVENT_MODEL_DATA;

@Component
public class RedisEventDataService {
    private final StringRedisTemplate stringRedisTemplate;
    @Qualifier("redisTaskAsyncPool")
    private final Executor redisTaskAsyncPool;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public RedisEventDataService(StringRedisTemplate stringRedisTemplate, Executor redisTaskAsyncPool) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.redisTaskAsyncPool = redisTaskAsyncPool;
    }

    @PostConstruct
    public void resetEventModelData() {
        stringRedisTemplate.delete(REDIS_KEY_EVENT_MODEL_DATA);
    }

    public Boolean isEventModelDataEmpty() {
        return  Boolean.FALSE.equals(stringRedisTemplate.hasKey(REDIS_KEY_EVENT_MODEL_DATA)) || Objects.requireNonNull(stringRedisTemplate.opsForHash().size(REDIS_KEY_EVENT_MODEL_DATA)) == 0;
    }

    public CompletableFuture<Boolean> storeEventModelData(Map<String, CarEventModel> carEventModelMap) {
        return CompletableFuture.supplyAsync(() -> {
            if (!isEventModelDataEmpty()) {
                stringRedisTemplate.delete(REDIS_KEY_EVENT_MODEL_DATA);
            }
            if (CollectionEmptyUtil.forMap(carEventModelMap)) { return false; }
            carEventModelMap.entrySet().stream().forEach(entry -> {
                stringRedisTemplate.opsForHash().put(REDIS_KEY_EVENT_MODEL_DATA, entry.getKey(), Objects.requireNonNull(SupplierUtils.resolve(() -> {
                    try { return objectMapper.writeValueAsString(entry.getValue()); }
                    catch (JsonProcessingException e) { return null; }
                })));
            });
            return true;
        },redisTaskAsyncPool);
    }

    public CompletableFuture<Map<String, CarEventModel>> collectEventModelData() {
        return CompletableFuture.supplyAsync(() -> {
            if (isEventModelDataEmpty()) { return Map.of(); }
            Map<String, CarEventModel> resultMap = new HashMap<>();
            stringRedisTemplate.opsForHash().entries(REDIS_KEY_EVENT_MODEL_DATA).entrySet().stream().forEach(entry -> {
                try {
                    String trajKey = String.valueOf(entry.getKey());
                    CarEventModel carEventModel = objectMapper.readValue(String.valueOf(entry.getValue()), CarEventModel.class);
                    resultMap.put(trajKey, carEventModel);
                } catch (JsonProcessingException ignored) {}
            });
            return resultMap;
        },redisTaskAsyncPool);
    }

}
