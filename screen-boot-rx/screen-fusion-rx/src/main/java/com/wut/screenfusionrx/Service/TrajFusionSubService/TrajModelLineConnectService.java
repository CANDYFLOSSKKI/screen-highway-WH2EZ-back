package com.wut.screenfusionrx.Service.TrajFusionSubService;

import com.wut.screencommonrx.Model.TrajModel;
import com.wut.screencommonrx.Model.TrajModelLine;
import com.wut.screencommonrx.Model.VehicleModel;
import com.wut.screencommonrx.Util.CollectionEmptyUtil;
import com.wut.screencommonrx.Util.ModelTransformUtil;
import com.wut.screendbredisrx.Service.RedisTrajFusionService;
import com.wut.screenfusionrx.Util.TrajModelParamUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.wut.screencommonrx.Static.FusionModuleStatic.*;

@Component
public class TrajModelLineConnectService {
    private final RedisTrajFusionService redisTrajFusionService;

    @Autowired
    public TrajModelLineConnectService(RedisTrajFusionService redisTrajFusionService) {
        this.redisTrajFusionService = redisTrajFusionService;
    }

    public void connect(List<VehicleModel> flushModelList, List<TrajModelLine> trajModelLineList) throws Exception{
        if (CollectionEmptyUtil.forList(flushModelList)) { return; }
        AtomicInteger carNumCountToWH = new AtomicInteger(0);
        AtomicInteger carNumCountToEZ = new AtomicInteger(0);
        flushModelList.sort(Comparator.comparingDouble(VehicleModel::getFrenetX));
        flushModelList.stream().forEach(model -> {
            TrajModel trajModel = ModelTransformUtil.vehicleToTraj(model);
            findMatchTrajModelLine(trajModel, model, trajModelLineList, carNumCountToWH, carNumCountToEZ);
        });
        redisTrajFusionService.storeTrajCarNumCount(carNumCountToWH.get(), carNumCountToEZ.get()).get(ASYNC_SERVICE_TIMEOUT, TimeUnit.SECONDS);
    }

    public void findMatchTrajModelLine(TrajModel trajModel, VehicleModel model, List<TrajModelLine> trajModelLineList, AtomicInteger carNumCountToWH, AtomicInteger carNumCountToEZ) {
        Optional<TrajModelLine> rawIdMatchOpt = trajModelLineList.stream().filter(trajModelLine -> {
            if (CollectionEmptyUtil.forList(trajModelLine.getTrajModels())) { return false; }
            return trajModelLine.getState() == 0 && Objects.equals(TrajModelParamUtil.getLineLastTrajModel(trajModelLine).getRawId(), model.getId());
        }).findFirst();
        if (rawIdMatchOpt.isPresent()) {
            setPriorityTrajModelLineParam(trajModel, rawIdMatchOpt.get());
            return;
        }
        Optional<TrajModelLine> frenetMatchOpt = trajModelLineList.stream().filter(trajModelLine -> {
            if (CollectionEmptyUtil.forList(trajModelLine.getTrajModels())) { return false; }
            TrajModel framTrajModel = TrajModelParamUtil.getLineLastTrajModel(trajModelLine);
            return trajModelLine.getState() == 0
                    && framTrajModel.getRoadDirect() == Integer.parseInt(model.getRoadDirect())
                    && Math.abs(framTrajModel.getFrenetXPrediction() - model.getFrenetX()) <= FUSION_DEL_X_TOLERANCE
                    && Math.abs(framTrajModel.getFrenetYPrediction() - model.getFrenetY()) <= FUSION_DEL_Y_TOLERANCE;
        }).min(Comparator.comparingDouble(trajModelLine -> {
            TrajModel framTrajModel = TrajModelParamUtil.getLineLastTrajModel(trajModelLine);
            double frenetXDisparity = Math.abs(framTrajModel.getFrenetXPrediction() - model.getFrenetX());
            double frenetYDisparity = Math.abs(framTrajModel.getFrenetYPrediction() - model.getFrenetY());
            return frenetXDisparity * FRENETX_PRIORITY_RADIO + frenetYDisparity * FRENETY_PRIORITY_RADIO;
        }));
        frenetMatchOpt.ifPresentOrElse(
            matchTrajModelLine -> setPriorityTrajModelLineParam(trajModel, matchTrajModelLine),
            () -> {
                setNewTrajModelLineParam(trajModel, trajModelLineList);
                switch (trajModel.getRoadDirect()) {
                    case ROAD_DIRECT_TO_EZ -> carNumCountToEZ.incrementAndGet();
                    case ROAD_DIRECT_TO_WH -> carNumCountToWH.incrementAndGet();
                }
            }
        );
    }

    public void setPriorityTrajModelLineParam(TrajModel trajModel, TrajModelLine trajModelLine) {
        trajModelLine.setState(1);
        trajModelLine.setEmptyFrameNum(0);
        TrajModel framTrajModel = TrajModelParamUtil.getLineLastTrajModel(trajModelLine);
        if (Objects.equals(trajModel.getCarId(), DEFAULT_CAR_ID)) {
            trajModel.setCarId(framTrajModel.getCarId());
            trajModel.setLicenseColor(framTrajModel.getLicenseColor());
        }
        trajModel.setRawId(framTrajModel.getRawId());
        trajModel.setTrajId(framTrajModel.getTrajId());
        trajModel.setLane(framTrajModel.getLane());
        double accxPattern = (trajModel.getSpeedX() - TrajModelParamUtil.getLineLastTrajModel(trajModelLine).getSpeedX()) * 1000 / FUSION_TIME_INTER;
        framTrajModel.setAccx(accxPattern);
        trajModelLine.getTrajModels().add(trajModel);
    }

    public void setNewTrajModelLineParam(TrajModel trajModel, List<TrajModelLine> trajModelLineList) {
        TrajModelLine trajModelLine = new TrajModelLine();
        trajModelLine.setState(2);
        trajModelLine.setEmptyFrameNum(0);
        trajModelLine.setCarIdTimestamp(0L);
        trajModelLine.setTrajModels(new ArrayList<>());
        long trajId = redisTrajFusionService.getAndIncrementNextTrajId() % 1000;
        trajModel.setTrajId((trajModel.getTimestamp() / 1000) * 1000 + trajId);
        if (Objects.equals(trajModel.getCarId(), DEFAULT_CAR_ID)) {
            trajModel.setCarId(TrajModelParamUtil.getTrajIdToPicLicense(trajId));
            trajModel.setLicenseColor(9);
        }
        trajModelLine.getTrajModels().add(trajModel);
        trajModelLineList.add(trajModelLine);
    }

}
