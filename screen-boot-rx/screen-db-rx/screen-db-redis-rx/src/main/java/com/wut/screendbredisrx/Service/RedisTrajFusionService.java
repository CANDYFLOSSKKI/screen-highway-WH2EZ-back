package com.wut.screendbredisrx.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wut.screencommonrx.Model.TrajModelLine;
import com.wut.screencommonrx.Util.CollectionEmptyUtil;
import com.wut.screendbmysqlrx.Model.Traj;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.function.SupplierUtils;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static com.wut.screencommonrx.Static.DbModuleStatic.*;

@Component
public class RedisTrajFusionService {
    private final StringRedisTemplate stringRedisTemplate;
    @Qualifier("redisTaskAsyncPool")
    private final Executor redisTaskAsyncPool;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public RedisTrajFusionService(StringRedisTemplate stringRedisTemplate, Executor redisTaskAsyncPool) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.redisTaskAsyncPool = redisTaskAsyncPool;
    }

    public Boolean isModelLineListEmpty() {
        return (Boolean.FALSE.equals(stringRedisTemplate.hasKey(REDIS_KEY_TRAJ_MODEL_LINE))) || Objects.requireNonNull(stringRedisTemplate.opsForList().size(REDIS_KEY_TRAJ_MODEL_LINE)).intValue() == 0;
    }

    public Boolean isTrajDataListEmpty() {
        return (Boolean.FALSE.equals(stringRedisTemplate.hasKey(REDIS_KEY_TRAJ_DATA))) || Objects.requireNonNull(stringRedisTemplate.opsForZSet().size(REDIS_KEY_TRAJ_DATA)).intValue() == 0;
    }

    @PostConstruct
    public void resetTrajFusion() {
        stringRedisTemplate.delete(REDIS_KEY_TRAJ_DATA);
        stringRedisTemplate.delete(REDIS_KEY_TRAJ_MODEL_LINE);
        stringRedisTemplate.opsForValue().set(REDIS_KEY_TRAJ_NEXT_ID, "1");
        stringRedisTemplate.opsForValue().set(REDIS_KEY_TRAJ_CAR_NUM_TO_EZ, "0");
        stringRedisTemplate.opsForValue().set(REDIS_KEY_TRAJ_CAR_NUM_TO_WH, "0");
    }

    public CompletableFuture<Boolean> storeTrajModelLine(List<TrajModelLine> list) {
        return CompletableFuture.supplyAsync(() -> {
            if (CollectionEmptyUtil.forList(list)) { return false; }
            for (TrajModelLine trajModelLine : list) {
                stringRedisTemplate.opsForList().rightPush(REDIS_KEY_TRAJ_MODEL_LINE, Objects.requireNonNull(SupplierUtils.resolve(() -> {
                    try { return objectMapper.writeValueAsString(trajModelLine); }
                    catch (JsonProcessingException e) { return null; }
                })));
            }
            return true;
        }, redisTaskAsyncPool);
    }

    public CompletableFuture<Void> storeTrajCarNumCount(int countToWH, int countToEZ) {
        return CompletableFuture.runAsync(() -> {
            stringRedisTemplate.opsForValue().set(REDIS_KEY_TRAJ_CAR_NUM_TO_EZ, Integer.toString(getTrajCarNumCountToEZ() + countToEZ));
            stringRedisTemplate.opsForValue().set(REDIS_KEY_TRAJ_CAR_NUM_TO_WH, Integer.toString(getTrajCarNumCountToWH() + countToWH));
        }, redisTaskAsyncPool);
    }

    public int getTrajCarNumCountToWH() {
        return Integer.parseInt(Objects.requireNonNull(stringRedisTemplate.opsForValue().get(REDIS_KEY_TRAJ_CAR_NUM_TO_WH)));
    }

    public int getTrajCarNumCountToEZ() {
        return Integer.parseInt(Objects.requireNonNull(stringRedisTemplate.opsForValue().get(REDIS_KEY_TRAJ_CAR_NUM_TO_EZ)));
    }

    public void resetTrajCarNumCount() {
        stringRedisTemplate.opsForValue().set(REDIS_KEY_TRAJ_CAR_NUM_TO_EZ, "0");
        stringRedisTemplate.opsForValue().set(REDIS_KEY_TRAJ_CAR_NUM_TO_WH, "0");
    }

    public CompletableFuture<List<TrajModelLine>> collectTrajModelLine() {
        return CompletableFuture.supplyAsync(() -> {
            if (isModelLineListEmpty()) { return List.of(); }
            return stringRedisTemplate.opsForList().leftPop(REDIS_KEY_TRAJ_MODEL_LINE, Objects.requireNonNull(stringRedisTemplate.opsForList().size(REDIS_KEY_TRAJ_MODEL_LINE))).stream().map(i -> {
                try { return objectMapper.readValue(i, TrajModelLine.class); }
                catch (JsonProcessingException e) { return null; }
            }).filter(Objects::nonNull).toList();
        },redisTaskAsyncPool);
    }

    public long getAndIncrementNextTrajId() {
        long trajId = Long.parseLong(Objects.requireNonNull(stringRedisTemplate.opsForValue().get(REDIS_KEY_TRAJ_NEXT_ID)));
        stringRedisTemplate.opsForValue().set(REDIS_KEY_TRAJ_NEXT_ID, Long.toString(trajId + 1));
        return trajId;
    }

    public CompletableFuture<Boolean> storeTrajData(List<Traj> trajList) {
        return CompletableFuture.supplyAsync(() -> {
            if (CollectionEmptyUtil.forList(trajList)) { return false; }
            trajList.stream().forEach(traj -> {
                stringRedisTemplate.opsForZSet().add(REDIS_KEY_TRAJ_DATA, Objects.requireNonNull(SupplierUtils.resolve(() -> {
                    try { return objectMapper.writeValueAsString(traj); }
                    catch (JsonProcessingException e) { return null; }
                })), traj.getTimestamp().doubleValue());
            });
            return true;
        },redisTaskAsyncPool);
    }

    public CompletableFuture<List<Traj>> collectTrajData(long timestamp) {
        return CompletableFuture.supplyAsync(() -> {
            if (isTrajDataListEmpty()) { return List.of(); }
            Set<String> trajStrSet = stringRedisTemplate.opsForZSet().rangeByScore(REDIS_KEY_TRAJ_DATA, (double)timestamp, (double)timestamp);
            if (CollectionEmptyUtil.forSet(trajStrSet)) { return List.of(); }
            return Objects.requireNonNull(trajStrSet).stream().map(str -> {
                try { return objectMapper.readValue(str, Traj.class); }
                catch (JsonProcessingException e) { return null; }
            }).filter(Objects::nonNull).toList();
        },redisTaskAsyncPool);
    }

    public CompletableFuture<List<Traj>> collectTrajData(long timestampStart, long timestampEnd) {
        return CompletableFuture.supplyAsync(() -> {
            if (isTrajDataListEmpty()) { return List.of(); }
            Set<String> trajStrSet = stringRedisTemplate.opsForZSet().rangeByScore(REDIS_KEY_TRAJ_DATA, (double)timestampStart, (double)timestampEnd);
            if (CollectionEmptyUtil.forSet(trajStrSet)) { return List.of(); }
            return Objects.requireNonNull(trajStrSet).stream().map(str -> {
                try { return objectMapper.readValue(str, Traj.class); }
                catch (JsonProcessingException e) { return null; }
            }).filter(Objects::nonNull).toList();
        },redisTaskAsyncPool);
    }

}
