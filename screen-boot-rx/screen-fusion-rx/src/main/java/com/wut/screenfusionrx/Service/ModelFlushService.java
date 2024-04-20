package com.wut.screenfusionrx.Service;

import com.wut.screencommonrx.Model.VehicleModel;
import com.wut.screencommonrx.Util.CollectionEmptyUtil;
import com.wut.screencommonrx.Util.DataParamParseUtil;
import com.wut.screencommonrx.Util.MessagePrintUtil;
import com.wut.screendbredisrx.Service.RedisModelDataService;
import com.wut.screendbredisrx.Service.RedisModelFlushService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import static com.wut.screencommonrx.Static.FusionModuleStatic.*;
import static com.wut.screencommonrx.Static.MsgModuleStatic.QUEUE_DEFAULT_EXCHANGE;
import static com.wut.screencommonrx.Static.MsgModuleStatic.QUEUE_NAME_FUSION;

@Component
public class ModelFlushService {
    private final RedisModelDataService redisModelDataService;
    private final RedisModelFlushService redisModelFlushService;
    @Qualifier("fusionTaskAsyncPool")
    private final Executor fusionTaskAsyncPool;
    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public ModelFlushService(RedisModelDataService redisModelDataService, RedisModelFlushService redisModelFlushService, RabbitTemplate rabbitTemplate, Executor fusionTaskAsyncPool) {
        this.redisModelDataService = redisModelDataService;
        this.redisModelFlushService = redisModelFlushService;
        this.rabbitTemplate = rabbitTemplate;
        this.fusionTaskAsyncPool = fusionTaskAsyncPool;
    }

    public List<VehicleModel> collectTargetModel(long timestamp) throws Exception{
        var flushModelTask = redisModelDataService.collectFiberModelData((double)timestamp);
        var laserModelTask = redisModelDataService.collectLaserModelData((double)timestamp);
        var waveModelTask = redisModelDataService.collectWaveModelData((double)timestamp);
        CompletableFuture.allOf(flushModelTask, laserModelTask, waveModelTask).get(ASYNC_SERVICE_TIMEOUT, TimeUnit.SECONDS);
        return flushDeduplication(flushModelTask.get(), laserModelTask.get(), waveModelTask.get());
    }

    public List<VehicleModel> flushDeduplication(List<VehicleModel> fiberModelList, List<VehicleModel> laserModelList, List<VehicleModel> waveModelList) {
        // 数据清洗前,对三种数据都按照id字段筛选,相同时间戳相同id的数据只能存在一个
        // 数据清洗前,对三种数据表中存在的null字段统一赋初始值
        if (CollectionEmptyUtil.forList(fiberModelList)) { return null; }
        if (!CollectionEmptyUtil.forList(laserModelList)) {
            laserModelList = laserModelList.stream().filter(DataParamParseUtil.modelDistinctByKey(VehicleModel::getId)).toList();
        }
        if (!CollectionEmptyUtil.forList(waveModelList, WAVE_MINIMUM_DATA_SIZE)) {
            waveModelList = waveModelListPreFlush(waveModelList);
        }
        fiberModelList = fiberModelList.stream().filter(DataParamParseUtil.modelDistinctByKey(VehicleModel::getId)).toList();
        fiberModelListWaveCalibration(fiberModelList, waveModelList);
        fiberModelListLaserCalibration(fiberModelList, laserModelList);
        fiberModelList.stream().forEach(vehicleModel -> {
            vehicleModel.setSpeed(Math.abs(vehicleModel.getSpeed()));
            vehicleModel.setSpeedX(Math.abs(vehicleModel.getSpeedX()));
            vehicleModel.setSpeedY(Math.abs(vehicleModel.getSpeedY()));
        });
        return fiberModelList;
    }

    public List<VehicleModel> waveModelListPreFlush(List<VehicleModel> modelList) {
        modelList = modelList.stream().filter(DataParamParseUtil.modelDistinctByKey(VehicleModel::getId)).sorted(Comparator.comparingDouble(VehicleModel::getFrenetX)).toList();
        VehicleModel standardModel = modelList.get(0);
        modelList = new ArrayList<>(modelList.stream().skip(1).filter(i ->
                Math.abs(i.getFrenetX() - standardModel.getFrenetX()) >= WAVE_FRENETX_TOLERANCE
                && Math.abs(i.getFrenetY() - standardModel.getFrenetY()) >= WAVE_FRENETY_TOLERANCE
                && Math.abs(i.getSpeed() - standardModel.getSpeed()) >= WAVE_SPEED_TOLERANCE
        ).toList());
        modelList.add(0, standardModel);
        return modelList;
    }

    public void fiberModelListWaveCalibration(List<VehicleModel> fiberList, List<VehicleModel> waveList) {
        if (CollectionEmptyUtil.forList(waveList)) { return; }
        List<Integer> fiberIdMarkList = new ArrayList<>();
        waveList.stream().forEach(wave -> {
            for (VehicleModel fiber : fiberList) {
                if (Math.abs(wave.getFrenetX() - fiber.getFrenetX()) < FRENETX_TOLERANCE
                        && Math.abs(wave.getFrenetY() - fiber.getFrenetY()) < FRENETY_TOLERANCE
                        && Objects.equals(wave.getRoadDirect(), fiber.getRoadDirect())
                        && (!fiberIdMarkList.contains(fiber.getId()))) {
                    fiberIdMarkList.add(fiber.getId());
                    fiberModelCalibration(fiber, wave);
                    break;
                }
            }
        });
    }

    public void fiberModelListLaserCalibration(List<VehicleModel> fiberList, List<VehicleModel> laserList) {
        if (CollectionEmptyUtil.forList(laserList)) { return; }
        List<Integer> fiberIdMarkList = new ArrayList<>();
        laserList.stream().forEach(laser -> {
            for (VehicleModel fiber : fiberList) {
                if (Objects.equals(fiber.getRoadDirect(), laser.getRoadDirect())
                        && Math.abs(fiber.getFrenetX() - laser.getFrenetX()) < FRENETX_TOLERANCE
                        && Math.abs(fiber.getFrenetY() - laser.getFrenetY()) < FRENETY_TOLERANCE
                        && (!fiberIdMarkList.contains(fiber.getId()))) {
                    fiberIdMarkList.add(fiber.getId());
                    fiberModelCalibration(fiber, laser);
                    break;
                }
            }
        });
    }

    public void fiberModelCalibration(VehicleModel target, VehicleModel source) {
        target.setSpeedX(source.getSpeedX());
        target.setSpeedY(source.getSpeedY());
        target.setSpeed(source.getSpeed());
        target.setLongitude(source.getLongitude());
        target.setLatitude(source.getLatitude());
        target.setMercatorX(source.getMercatorX());
        target.setMercatorY(source.getMercatorY());
        target.setFrenetX(source.getFrenetX());
        target.setFrenetY(source.getFrenetY());
        target.setHeadingAngle(source.getHeadingAngle());
        target.setFrenetAngle(source.getFrenetAngle());
    }

    public CompletableFuture<Void> storeFlushModel(List<VehicleModel> list, long timestamp) {
        return CompletableFuture.runAsync(() -> {
            redisModelFlushService.storeFlushVehicleModel(list).thenRunAsync(() -> {
                MessagePrintUtil.printModelFlush(timestamp);
                rabbitTemplate.convertAndSend(QUEUE_DEFAULT_EXCHANGE, QUEUE_NAME_FUSION, String.valueOf(timestamp));
            });
        },fusionTaskAsyncPool);
    }

}
