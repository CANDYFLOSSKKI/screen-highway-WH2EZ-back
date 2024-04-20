package com.wut.screenmsgtx.Service.AsyncService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wut.screencommontx.Entity.TransmitDataParam;
import com.wut.screencommontx.Util.CollectionEmptyUtil;
import com.wut.screencommontx.Util.MessagePrintUtil;
import com.wut.screendbtx.Model.Fiber;
import com.wut.screendbtx.Model.Laser;
import com.wut.screendbtx.Model.Plate;
import com.wut.screendbtx.Model.Wave;
import com.wut.screenmsgtx.Context.MsgRedisDataContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static com.wut.screencommontx.Static.MsgModuleStatic.*;

@Component
public class TransmitDataAsyncService {
    private final MsgRedisDataContext msgRedisDataContext;
    private final StringRedisTemplate stringRedisTemplate;
    private final KafkaTemplate kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Qualifier("msgTaskAsyncPool")
    private final Executor msgTaskAsyncPool;

    @Autowired
    public TransmitDataAsyncService(MsgRedisDataContext msgRedisDataContext, StringRedisTemplate stringRedisTemplate, KafkaTemplate kafkaTemplate, Executor msgTaskAsyncPool) {
        this.msgRedisDataContext = msgRedisDataContext;
        this.stringRedisTemplate = stringRedisTemplate;
        this.kafkaTemplate = kafkaTemplate;
        this.msgTaskAsyncPool = msgTaskAsyncPool;
    }

    public <T> String getWrapSendDataStr(double timestamp, T data) {
        TransmitDataParam dataToSend = new TransmitDataParam((long)timestamp, data);
        try { return objectMapper.writeValueAsString(dataToSend); }
        catch (JsonProcessingException e) { return null; }
    }

    public <T> void storeCollectDataWithNoAsync(String key, List<T> data) {
        stringRedisTemplate.opsForList().leftPushAll(key, data.stream().map(i -> {
            try { return objectMapper.writeValueAsString(i); }
            catch (JsonProcessingException e) { return null; }
        }).toList());
    }

    public CompletableFuture<List<Plate>> filterCollectPlateData(double timestamp) {
        return CompletableFuture.supplyAsync(() -> {
            if (msgRedisDataContext.isDataListEmpty(REDIS_KEY_PLATE_DATA)) { return List.of(); }
            List<Plate> plateData = new ArrayList<>(Objects.requireNonNull(stringRedisTemplate.opsForList().leftPop(REDIS_KEY_PLATE_DATA, msgRedisDataContext.getDataListSize(REDIS_KEY_PLATE_DATA))).stream().map(str -> {
                try { return objectMapper.readValue(str, Plate.class); }
                catch (JsonProcessingException e) { return null; }
            }).filter(Objects::nonNull).toList());
            List<Plate> plateMeetTimestamp = plateData.stream().filter(i -> i.getGlobalTimestamp() == null || i.getGlobalTimestamp() <= timestamp).toList();
            plateData.removeAll(plateMeetTimestamp);
            if (!plateData.isEmpty()) {
                storeCollectDataWithNoAsync(REDIS_KEY_PLATE_DATA, plateData);
            }
            return plateMeetTimestamp;
        },msgTaskAsyncPool);
    }

    public CompletableFuture<List<Fiber>> filterCollectFiberData(double timestamp) {
        return CompletableFuture.supplyAsync(() -> {
            if (msgRedisDataContext.isDataListEmpty(REDIS_KEY_FIBER_DATA)) { return List.of(); }
            List<Fiber> fiberData = new ArrayList<>(Objects.requireNonNull(stringRedisTemplate.opsForList().leftPop(REDIS_KEY_FIBER_DATA, msgRedisDataContext.getDataListSize(REDIS_KEY_FIBER_DATA))).stream().map(str -> {
                try { return objectMapper.readValue(str, Fiber.class); }
                catch (JsonProcessingException e) { return null; }
            }).filter(Objects::nonNull).toList());
            List<Fiber> fiberMeetTimestamp = fiberData.stream().filter(i -> i.getTimestamp() == null || i.getTimestamp() <= timestamp).toList();
            fiberData.removeAll(fiberMeetTimestamp);
            if (!fiberData.isEmpty()) {
                storeCollectDataWithNoAsync(REDIS_KEY_FIBER_DATA, fiberData);
            }
            return fiberMeetTimestamp;
        },msgTaskAsyncPool);
    }

    public CompletableFuture<List<Laser>> filterCollectLaserData(double timestamp) {
        return CompletableFuture.supplyAsync(() -> {
            if (msgRedisDataContext.isDataListEmpty(REDIS_KEY_LASER_DATA)) { return List.of(); }
            List<Laser> laserData = new ArrayList<>(Objects.requireNonNull(stringRedisTemplate.opsForList().leftPop(REDIS_KEY_LASER_DATA, msgRedisDataContext.getDataListSize(REDIS_KEY_LASER_DATA))).stream().map(str -> {
                try { return objectMapper.readValue(str, Laser.class); }
                catch (JsonProcessingException e) { return null; }
            }).filter(Objects::nonNull).toList());
            List<Laser> laserMeetTimestamp = laserData.stream().filter(i -> i.getTimestamp() == null || i.getTimestamp() <= timestamp).toList();
            laserData.removeAll(laserMeetTimestamp);
            if (!laserData.isEmpty()) {
                storeCollectDataWithNoAsync(REDIS_KEY_LASER_DATA, laserData);
            }
            return laserMeetTimestamp;
        },msgTaskAsyncPool);
    }

    public CompletableFuture<List<Wave>> filterCollectWaveData(double timestamp) {
        return CompletableFuture.supplyAsync(() -> {
            if (msgRedisDataContext.isDataListEmpty(REDIS_KEY_WAVE_DATA)) { return List.of(); }
            List<Wave> waveData = new ArrayList<>(Objects.requireNonNull(stringRedisTemplate.opsForList().leftPop(REDIS_KEY_WAVE_DATA, msgRedisDataContext.getDataListSize(REDIS_KEY_WAVE_DATA))).stream().map(str -> {
                try { return objectMapper.readValue(str, Wave.class); }
                catch (JsonProcessingException e) { return null; }
            }).filter(Objects::nonNull).toList());
            List<Wave> waveMeetTimestamp = waveData.stream().filter(i -> i.getTimestamp() == null || i.getTimestamp() <= timestamp).toList();
            waveData.removeAll(waveMeetTimestamp);
            if (!waveData.isEmpty()) {
                storeCollectDataWithNoAsync(REDIS_KEY_WAVE_DATA, waveData);
            }
            return waveMeetTimestamp;
        },msgTaskAsyncPool);
    }

    public CompletableFuture<Boolean> transmitPlateData(List<Plate> plateData, double timestamp) {
        return CompletableFuture.supplyAsync(() -> {
            if (CollectionEmptyUtil.forList(plateData)) { return false; }
            plateData.stream().forEach(plate -> {
                String sendDataStr = getWrapSendDataStr(timestamp,plate);
                if (sendDataStr != null) {
                    kafkaTemplate.send(TOPIC_NAME_PLATE, sendDataStr);
                    MessagePrintUtil.printProducerTransmit(TOPIC_NAME_PLATE, sendDataStr);
                }
            });
            return true;
        },msgTaskAsyncPool);
    }

    public CompletableFuture<Boolean> transmitFiberData(List<Fiber> fiberData, double timestamp) {
        return CompletableFuture.supplyAsync(() -> {
            if (CollectionEmptyUtil.forList(fiberData)) { return false; }
            fiberData.stream().forEach(fiber -> {
                String sendDataStr = getWrapSendDataStr(timestamp,fiber);
                if (sendDataStr != null) {
                    kafkaTemplate.send(TOPIC_NAME_FIBER, sendDataStr);
                    MessagePrintUtil.printProducerTransmit(TOPIC_NAME_FIBER, sendDataStr);
                }
            });
            return true;
        },msgTaskAsyncPool);
    }

    public CompletableFuture<Boolean> transmitLaserData(List<Laser> laserData, double timestamp) {
        return CompletableFuture.supplyAsync(() -> {
            if (CollectionEmptyUtil.forList(laserData)) { return false; }
            laserData.stream().forEach(laser -> {
                String sendDataStr = getWrapSendDataStr(timestamp,laser);
                if (sendDataStr != null) {
                    kafkaTemplate.send(TOPIC_NAME_LASER, sendDataStr);
                    MessagePrintUtil.printProducerTransmit(TOPIC_NAME_LASER, sendDataStr);
                }
            });
            return true;
        },msgTaskAsyncPool);
    }

    public CompletableFuture<Boolean> transmitWaveData(List<Wave> waveData, double timestamp) {
        return CompletableFuture.supplyAsync(() -> {
            if (CollectionEmptyUtil.forList(waveData)) { return false; }
            waveData.stream().forEach(wave -> {
                String sendDataStr = getWrapSendDataStr(timestamp,wave);
                if (sendDataStr != null) {
                    kafkaTemplate.send(TOPIC_NAME_WAVE, sendDataStr);
                    MessagePrintUtil.printProducerTransmit(TOPIC_NAME_WAVE, sendDataStr);
                }
            });
            return true;
        },msgTaskAsyncPool);
    }

    public CompletableFuture<Void> transmitTimestamp(double timestamp) {
        return CompletableFuture.runAsync(() -> {
            kafkaTemplate.send(TOPIC_NAME_TIMESTAMP, String.valueOf(((long)timestamp)));
            MessagePrintUtil.printProducerTransmit(TOPIC_NAME_TIMESTAMP,String.valueOf(((long)timestamp)));
        },msgTaskAsyncPool);
    }

}
