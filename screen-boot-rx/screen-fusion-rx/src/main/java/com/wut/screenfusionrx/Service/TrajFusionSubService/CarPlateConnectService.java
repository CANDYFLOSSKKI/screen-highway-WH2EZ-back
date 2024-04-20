package com.wut.screenfusionrx.Service.TrajFusionSubService;

import com.wut.screencommonrx.Model.CarPlateModel;
import com.wut.screencommonrx.Model.TrajModel;
import com.wut.screencommonrx.Model.TrajModelLine;
import com.wut.screencommonrx.Util.CollectionEmptyUtil;
import com.wut.screencommonrx.Util.MessagePrintUtil;
import com.wut.screendbredisrx.Service.RedisModelDataService;
import com.wut.screenfusionrx.Util.TrajModelParamUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static com.wut.screencommonrx.Static.FusionModuleStatic.*;

@Component
public class CarPlateConnectService {
    private final RedisModelDataService redisModelDataService;

    @Autowired
    public CarPlateConnectService(RedisModelDataService redisModelDataService) {
        this.redisModelDataService = redisModelDataService;
    }

    public void connect(List<TrajModelLine> trajModelLineList, double realTimestamp) throws Exception{
        if (CollectionEmptyUtil.forList(trajModelLineList)) { return; }
        // 牌照数据的接收时间戳均以X000结尾,格式化时间戳
        double timestamp = (realTimestamp / 1000) * 1000 + 1000;
        // (精确匹配)获取当前时间戳存储的牌照信息
        var carPlateTask = redisModelDataService.collectPlateModelData(timestamp, timestamp);
        // (模糊匹配)获取当前时间戳向后5s范围内存储的牌照信息
        var carPlateRangeTask = redisModelDataService.collectPlateModelData(timestamp - 5000, timestamp - 1);
        CompletableFuture.allOf(carPlateTask, carPlateRangeTask).get(ASYNC_SERVICE_TIMEOUT, TimeUnit.SECONDS);
        List<CarPlateModel> readyToMoveList = new ArrayList<>();
        // 精确匹配调用
        if (!CollectionEmptyUtil.forList(carPlateTask.get())) {
            List<CarPlateModel> carPlateModelListNow = new ArrayList<>(carPlateTask.get());
            preciseMatchCarPlate(carPlateModelListNow, trajModelLineList, timestamp, readyToMoveList);
        }
        // 模糊匹配调用
        if (!CollectionEmptyUtil.forList(carPlateRangeTask.get())) {
            List<CarPlateModel> carPlateModelListRange = new ArrayList<>(carPlateRangeTask.get());
            fuzzyMatchCarPlate(carPlateModelListRange, trajModelLineList, timestamp, realTimestamp, readyToMoveList);
        }
        if (!CollectionEmptyUtil.forList(readyToMoveList)) {
            redisModelDataService.removePlateModelData(readyToMoveList);
        }
    }

    public void preciseMatchCarPlate(List<CarPlateModel> carPlateModelList, List<TrajModelLine> trajModelLineList, double timestamp, List<CarPlateModel> readyToMoveList) {
        carPlateModelList.stream().forEach(carPlateModel -> {
            Optional<TrajModelLine> preciseTrajOpt = trajModelLineList.stream().filter(trajModelLine -> {
                TrajModel framTrajModel = TrajModelParamUtil.getLineLastTrajModel(trajModelLine);
                return trajModelLine.getState() != 0
                        &&(carPlateModel.getRoadDirect() == null || carPlateModel.getRoadDirect().isEmpty() || framTrajModel.getRoadDirect() == Integer.parseInt(carPlateModel.getRoadDirect()))
                        && (carPlateModel.getStart() == null || carPlateModel.getStart().isEmpty() || Double.parseDouble(carPlateModel.getStart()) <= framTrajModel.getFrenetX())
                        && (carPlateModel.getEnd() == null || carPlateModel.getEnd().isEmpty() || Double.parseDouble(carPlateModel.getEnd()) >= framTrajModel.getFrenetX())
                        && Math.abs(timestamp - trajModelLine.getCarIdTimestamp().doubleValue()) >= CAR_CONNECT_TIME_GAP;
            }).min(Comparator.comparingDouble(trajModelLine -> {
                double frenetX = TrajModelParamUtil.getLineLastTrajModel(trajModelLine).getFrenetX();
                double carId = 0.0;
                if (carPlateModel.getStart() != null && !carPlateModel.getStart().isEmpty() && carPlateModel.getEnd() != null && !carPlateModel.getEnd().isEmpty()) {
                    carId = (Double.parseDouble(carPlateModel.getStart()) + Double.parseDouble(carPlateModel.getEnd())) / 2;
                }
                return Math.abs(frenetX - carId);
            }));
            preciseTrajOpt.ifPresent(trajModelLine -> {
                setTrajParam(carPlateModel, trajModelLine, timestamp);
                readyToMoveList.add(carPlateModel);
            });
        });
    }

    public void fuzzyMatchCarPlate(List<CarPlateModel> carPlateModelList, List<TrajModelLine> trajModelLineList, double timestamp, double realTimestamp, List<CarPlateModel> readyToMoveList) {
        trajModelLineList.stream().filter(trajModelLine -> trajModelLine.getState() != 0 && trajModelLine.getCarIdTimestamp() < timestamp ).forEach(trajModelLine -> {
            TrajModel framTrajModel = TrajModelParamUtil.getLineLastTrajModel(trajModelLine);
            Optional<CarPlateModel> fuzzyCarPlateOpt = carPlateModelList.stream().filter(carPlateModel ->
                trajModelLine.getState() != 0
                 && (carPlateModel.getRoadDirect() == null || carPlateModel.getRoadDirect().isEmpty() || framTrajModel.getRoadDirect() == Integer.parseInt(carPlateModel.getRoadDirect()))
                 && (carPlateModel.getStart() == null || carPlateModel.getStart().isEmpty() || Double.parseDouble(carPlateModel.getStart()) - CAR_CONNECT_FRENETX_OFFSET <= framTrajModel.getFrenetX())
                 && (carPlateModel.getEnd() == null || carPlateModel.getEnd().isEmpty() || Double.parseDouble(carPlateModel.getEnd()) + CAR_CONNECT_FRENETX_OFFSET >= framTrajModel.getFrenetX())
                 && Math.abs(timestamp - trajModelLine.getCarIdTimestamp()) >= CAR_CONNECT_TIME_GAP
            ).min(Comparator.comparingDouble(carPlateModel -> {
                double carPlateTimeGap = Math.abs(realTimestamp - carPlateModel.getTimestamp().doubleValue()) / 1000;
                double carIdLocation = 0.0;
                if (carPlateModel.getStart() != null && !carPlateModel.getStart().isEmpty() && carPlateModel.getEnd() != null && !carPlateModel.getEnd().isEmpty()) {
                    carIdLocation = (Double.parseDouble(carPlateModel.getStart()) + Double.parseDouble(carPlateModel.getEnd())) / 2;
                }
                return Math.abs(carIdLocation - (framTrajModel.getFrenetX() / carPlateTimeGap) - Math.abs(framTrajModel.getSpeedX()));
            }));
            fuzzyCarPlateOpt.ifPresent(carPlateModel -> {
                setTrajParam(carPlateModel, trajModelLine, timestamp);
                readyToMoveList.add(carPlateModel);
            });
        });
    }

    public void setTrajParam(CarPlateModel carPlateModel, TrajModelLine trajModelLine, double timestamp) {
        trajModelLine.setCarIdTimestamp((long)timestamp);
        TrajModel trajModel = TrajModelParamUtil.getLineLastTrajModel(trajModelLine);
        MessagePrintUtil.printCarPlateBind(trajModel.getCarId(), carPlateModel.getPicLicense());
        trajModel.setCarId(carPlateModel.getPicLicense());
        trajModel.setLane(carPlateModel.getLaneNum());
        trajModel.setLicenseColor(carPlateModel.getLicenseColor());
    }

}
