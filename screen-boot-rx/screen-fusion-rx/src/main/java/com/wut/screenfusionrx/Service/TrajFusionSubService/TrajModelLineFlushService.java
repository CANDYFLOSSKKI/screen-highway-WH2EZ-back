package com.wut.screenfusionrx.Service.TrajFusionSubService;

import com.wut.screencommonrx.Model.TrajModel;
import com.wut.screencommonrx.Model.TrajModelLine;
import com.wut.screencommonrx.Util.CollectionEmptyUtil;
import com.wut.screencommonrx.Util.ModelTransformUtil;
import com.wut.screendbmysqlrx.Model.Traj;
import com.wut.screendbmysqlrx.Model.TrajCarPlate;
import com.wut.screendbmysqlrx.Service.TrajCarPlateService;
import com.wut.screendbmysqlrx.Util.DbModelTransformUtil;
import com.wut.screenfusionrx.Util.TrajModelParamUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Component
public class TrajModelLineFlushService {
    private final TrajCarPlateService trajCarPlateService;
    @Qualifier("fusionTaskAsyncPool")
    private final Executor fusionTaskAsyncPool;

    @Autowired
    public TrajModelLineFlushService(TrajCarPlateService trajCarPlateService, Executor fusionTaskAsyncPool) {
        this.trajCarPlateService = trajCarPlateService;
        this.fusionTaskAsyncPool = fusionTaskAsyncPool;
    }

    public List<Traj> collectTrajData(List<TrajModelLine> trajModelLineList, long timestamp) {
        if (CollectionEmptyUtil.forList(trajModelLineList)) { return null; }
        trajModelLineList.stream().filter(trajModelLine -> trajModelLine.getState() == 0).forEach(trajModelLine -> {
            TrajModel framTrajModel = TrajModelParamUtil.getLineLastTrajModel(trajModelLine);
            TrajModel readyToAddTrajModel = ModelTransformUtil.trajModelToFrame(framTrajModel, timestamp);
            trajModelLine.getTrajModels().add(readyToAddTrajModel);
            trajModelLine.setEmptyFrameNum(trajModelLine.getEmptyFrameNum() + 1);
        });
        return filterDisposeData(trajModelLineList).stream().map(DbModelTransformUtil::trajModelToTraj).toList();
    }

    public List<TrajModel> filterDisposeData(List<TrajModelLine> trajModelLineList) {
        List<TrajModelLine> readyToMoveList = new ArrayList<>();
        List<TrajModel> readyToStoreList = new ArrayList<>();
        List<TrajCarPlate> readyToDepList = new ArrayList<>();
        trajModelLineList.stream().filter(trajModelLine -> TrajModelParamUtil.getLineTrajModelSize(trajModelLine) == 4).forEach(trajModelLine -> {
            // 出现连续的三个空轨迹数据帧时,首个元素一定是有效帧
            if (trajModelLine.getEmptyFrameNum() == 3) {
                readyToMoveList.add(trajModelLine);
                readyToDepList.add(DbModelTransformUtil.trajModelToCarPlate(trajModelLine.getTrajModels().get(0)));
                return;
            }
            TrajModel trajModel = trajModelLine.getTrajModels().remove(0);
            readyToStoreList.add(trajModel);
        });
        CompletableFuture.runAsync(() -> {
            trajCarPlateService.storeTrajCarPlate(readyToDepList);
        }, fusionTaskAsyncPool);
        trajModelLineList.removeAll(readyToMoveList);
        trajModelLineList.stream().forEach(trajModelLine -> trajModelLine.setState(0));
        return readyToStoreList;
    }

}
