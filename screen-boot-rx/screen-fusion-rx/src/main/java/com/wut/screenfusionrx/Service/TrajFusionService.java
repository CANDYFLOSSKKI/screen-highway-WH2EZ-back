package com.wut.screenfusionrx.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wut.screencommonrx.Model.TrajModelLine;
import com.wut.screencommonrx.Model.VehicleModel;
import com.wut.screencommonrx.Util.CollectionEmptyUtil;
import com.wut.screencommonrx.Util.MessagePrintUtil;
import com.wut.screendbmysqlrx.Model.Traj;
import com.wut.screendbredisrx.Service.RedisBatchCacheService;
import com.wut.screendbredisrx.Service.RedisModelFlushService;
import com.wut.screendbredisrx.Service.RedisTrajFusionService;
import com.wut.screenfusionrx.Model.TrajFrameModel;
import com.wut.screenfusionrx.Service.TrajFusionSubService.CarPlateConnectService;
import com.wut.screenfusionrx.Service.TrajFusionSubService.TrajModelLineConnectService;
import com.wut.screenfusionrx.Service.TrajFusionSubService.TrajModelLineFlushService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.wut.screencommonrx.Static.FusionModuleStatic.ASYNC_SERVICE_TIMEOUT;
import static com.wut.screencommonrx.Static.FusionModuleStatic.TRAJ_DATA_TIME_OFFSET;
import static com.wut.screencommonrx.Static.MsgModuleStatic.*;

@Component
public class TrajFusionService {
    private final RedisModelFlushService redisModelFlushService;
    private final RedisTrajFusionService redisTrajFusionService;
    private final CarPlateConnectService carPlateConnectService;
    private final TrajModelLineConnectService trajModelLineConnectService;
    private final TrajModelLineFlushService trajModelLineFlushService;
    private final RedisBatchCacheService redisBatchCacheService;
    private final KafkaTemplate kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Qualifier("fusionTaskAsyncPool")
    private final Executor fusionTaskAsyncPool;
    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public TrajFusionService(RedisModelFlushService redisModelFlushService, RedisTrajFusionService redisTrajFusionService, CarPlateConnectService carPlateConnectService, TrajModelLineConnectService trajModelLineConnectService, TrajModelLineFlushService trajModelLineFlushService, RabbitTemplate rabbitTemplate, Executor fusionTaskAsyncPool, RedisBatchCacheService redisBatchCacheService, KafkaTemplate kafkaTemplate) {
        this.redisModelFlushService = redisModelFlushService;
        this.redisTrajFusionService = redisTrajFusionService;
        this.carPlateConnectService = carPlateConnectService;
        this.trajModelLineConnectService = trajModelLineConnectService;
        this.trajModelLineFlushService = trajModelLineFlushService;
        this.fusionTaskAsyncPool = fusionTaskAsyncPool;
        this.rabbitTemplate = rabbitTemplate;
        this.redisBatchCacheService = redisBatchCacheService;
        this.kafkaTemplate = kafkaTemplate;
    }

    public List<Traj> collectFusionTraj(long timestamp) throws Exception{
        var flushModelTask = redisModelFlushService.collectFlushVehicleModel((double)timestamp);
        var trajModelLineTask = redisTrajFusionService.collectTrajModelLine();
        CompletableFuture.allOf(flushModelTask,trajModelLineTask).get(ASYNC_SERVICE_TIMEOUT,TimeUnit.SECONDS);
        List<TrajModelLine> trajModelLineList = new ArrayList<>();
        if (!CollectionEmptyUtil.forList(trajModelLineTask.get())) {
            trajModelLineList.addAll(trajModelLineTask.get());
        }
        if (!CollectionEmptyUtil.forList(flushModelTask.get())) {
            List<VehicleModel> flushModelList = new ArrayList<>(flushModelTask.get());
            trajModelLineConnectService.connect(flushModelList, trajModelLineList);
            carPlateConnectService.connect(trajModelLineList, (double)timestamp);
        }
        List<Traj> trajList = trajModelLineFlushService.collectTrajData(trajModelLineList, timestamp);
        redisTrajFusionService.storeTrajModelLine(trajModelLineList).get(ASYNC_SERVICE_TIMEOUT, TimeUnit.SECONDS);
        return trajList;
    }

    public CompletableFuture<Void> storeTrajListData(List<Traj> trajList, long timestamp) throws Exception{
        return CompletableFuture.runAsync(() -> {
            redisTrajFusionService.storeTrajData(trajList).thenRunAsync(() -> {
                MessagePrintUtil.printTrajFlush(timestamp);
                String sendTimeStr = String.valueOf(timestamp - TRAJ_DATA_TIME_OFFSET);
                rabbitTemplate.convertAndSend(QUEUE_DEFAULT_EXCHANGE, QUEUE_NAME_EVENT, sendTimeStr);
                rabbitTemplate.convertAndSend(QUEUE_DEFAULT_EXCHANGE, QUEUE_NAME_SECTION, sendTimeStr);
                rabbitTemplate.convertAndSend(QUEUE_DEFAULT_EXCHANGE, QUEUE_NAME_POSTURE, sendTimeStr);
            });
            redisBatchCacheService.storeTrajCache(trajList, timestamp - TRAJ_DATA_TIME_OFFSET);
        }, fusionTaskAsyncPool);
    }

    public CompletableFuture<Void> sendTrajListData(List<Traj> trajList, long timestamp){
        return CompletableFuture.runAsync(() -> {
            try {
                AtomicInteger carNumCountToWH = new AtomicInteger(0);
                AtomicInteger carNumCountToEZ = new AtomicInteger(0);
                CompletableFuture.allOf(
                    CompletableFuture.runAsync(() -> carNumCountToWH.compareAndExchange(0, redisTrajFusionService.getTrajCarNumCountToWH()), fusionTaskAsyncPool),
                    CompletableFuture.runAsync(() -> carNumCountToEZ.compareAndExchange(0, redisTrajFusionService.getTrajCarNumCountToEZ()), fusionTaskAsyncPool)
                ).get(ASYNC_SERVICE_TIMEOUT, TimeUnit.SECONDS);
                String trajFrameModelStr = objectMapper.writeValueAsString(new TrajFrameModel(timestamp - TRAJ_DATA_TIME_OFFSET, carNumCountToWH.get(), carNumCountToEZ.get(), trajList));
                kafkaTemplate.send(TOPIC_NAME_TRAJ, trajFrameModelStr);
                MessagePrintUtil.printProducerTransmit(TOPIC_NAME_TRAJ, trajFrameModelStr);
            } catch (Exception e) { e.printStackTrace(); }
        }, fusionTaskAsyncPool);
    }

}
