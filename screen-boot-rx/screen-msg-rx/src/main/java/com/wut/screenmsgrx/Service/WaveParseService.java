package com.wut.screenmsgrx.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wut.screencommonrx.Model.MsgSendDataModel.MsgSendData;
import com.wut.screencommonrx.Model.MsgSendDataModel.Wave;
import com.wut.screencommonrx.Util.ModelTransformUtil;
import com.wut.screendbredisrx.Service.RedisModelDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.function.SupplierUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static com.wut.screencommonrx.Static.MsgModuleStatic.*;


@Component
public class WaveParseService {
    private final RedisModelDataService redisModelDataService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Qualifier("msgTaskAsyncPool")
    private final Executor msgTaskAsyncPool;

    @Autowired
    public WaveParseService(RedisModelDataService redisModelDataService, Executor msgTaskAsyncPool) {
        this.redisModelDataService = redisModelDataService;
        this.msgTaskAsyncPool = msgTaskAsyncPool;
    }

    public CompletableFuture<Void> collectWaveData(String waveDataStr) {
        return CompletableFuture.runAsync(() -> {
            MsgSendData msgSendData = SupplierUtils.resolve(() -> {
                try { return objectMapper.readValue(waveDataStr, MsgSendData.class); }
                catch (JsonProcessingException e) { return null; }
            });
            if (msgSendData != null) {
                Wave waveData = objectMapper.convertValue(msgSendData.getData(), Wave.class);
                List<Double> frenetPosition = getWaveFrenetPosition(waveData.getIp(),waveData.getFrenetX(),waveData.getFrenetY());
                redisModelDataService.storeWaveModelData(
                    ModelTransformUtil.waveToVehicle(waveData, msgSendData.getTimestamp(), frenetPosition)
                ).thenRunAsync(() -> {});
            }
        },msgTaskAsyncPool);
    }

    public List<Double> getWaveFrenetPosition(String ip, Double frenetX, Double frenetY) {
        List<Double> frenetPositionList = new ArrayList<>();
        if (frenetX == null || frenetY == null) {
            frenetPositionList.add(frenetX);
            frenetPositionList.add(frenetY);
            return frenetPositionList;
        }
        switch (ip) {
            case WAVE_IP_SELECTION_1ST -> {
                frenetPositionList.add((frenetX - 7479.768) * Math.cos(Math.atan(-0.02395382)) - (frenetY - 8.021161) * Math.sin(Math.atan(-0.02395382)) + 7479.768 + 1.5079);
                frenetPositionList.add((frenetX - 7479.768) * Math.sin(Math.atan(-0.02395382)) - (frenetY - 8.021161) * Math.cos(Math.atan(-0.02395382)) + 8.021161 - 1.1247);
            }
            case WAVE_IP_SELECTION_2ND -> {
                frenetPositionList.add((frenetX - 9439.862) * Math.cos(Math.atan(0.00532761)) - (frenetY - 6.714382) * Math.sin(Math.atan(0.00532761)) + 9439.862 + 4.3058);
                frenetPositionList.add((frenetX - 9439.862) * Math.sin(Math.atan(0.00532761)) + (frenetY - 6.714382) * Math.cos(Math.atan(0.00532761)) + 6.714382 + 0.4208);
            }
            case WAVE_IP_SELECTION_3RD -> {
                frenetPositionList.add((frenetX - 10421.64) * Math.cos(Math.atan(0.0090833)) - (frenetY - 6.146297) * Math.sin(Math.atan(0.0090833)) + 10421.64 + 0.78622);
                frenetPositionList.add((frenetX - 10421.64) * Math.sin(Math.atan(0.0090833)) - (frenetY - 6.146297) * Math.cos(Math.atan(0.0090833)) + 6.146297 + 1.33396);
            }
            case WAVE_IP_SELECTION_4TH -> {
                frenetPositionList.add((frenetX - 11373.59) * Math.cos(Math.atan(0.01706676)) - (frenetY - 5.491768) * Math.sin(Math.atan(0.01706676)) + 11373.59 - 0.601865);
                frenetPositionList.add((frenetX - 11373.59) * Math.sin(Math.atan(0.01706676)) - (frenetY - 5.491768) * Math.cos(Math.atan(0.01706676)) + 5.491768 + 1.38588);
            }
            default -> {
                frenetPositionList.add(frenetX);
                frenetPositionList.add(frenetY);
            }
        }
        return frenetPositionList;
    }

}
