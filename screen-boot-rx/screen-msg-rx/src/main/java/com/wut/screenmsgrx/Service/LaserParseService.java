package com.wut.screenmsgrx.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wut.screencommonrx.Model.MsgSendDataModel.Laser;
import com.wut.screencommonrx.Model.MsgSendDataModel.MsgSendData;
import com.wut.screencommonrx.Util.ModelTransformUtil;
import com.wut.screendbredisrx.Service.RedisModelDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.function.SupplierUtils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Component
public class LaserParseService {
    private final RedisModelDataService redisModelDataService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Qualifier("msgTaskAsyncPool")
    private final Executor msgTaskAsyncPool;

    @Autowired
    public LaserParseService(RedisModelDataService redisModelDataService, Executor msgTaskAsyncPool) {
        this.redisModelDataService = redisModelDataService;
        this.msgTaskAsyncPool = msgTaskAsyncPool;
    }

    public CompletableFuture<Void> collectLaserData(String laserDataStr){
        return CompletableFuture.runAsync(() -> {
            MsgSendData msgSendData = SupplierUtils.resolve(() -> {
                try { return objectMapper.readValue(laserDataStr, MsgSendData.class); }
                catch (JsonProcessingException e) { return null; }
            });
            if (msgSendData != null) {
                Laser laserData = objectMapper.convertValue(msgSendData.getData(),Laser.class);
                redisModelDataService.storeLaserModelData(
                    ModelTransformUtil.laserToVehicle(laserData, msgSendData.getTimestamp())
                ).thenRunAsync(() -> {});
            }
        },msgTaskAsyncPool);
    }

}
