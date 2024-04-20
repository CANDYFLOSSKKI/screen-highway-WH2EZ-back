package com.wut.screenfusionrx.Service;

import com.wut.screencommonrx.Entity.TrajRecordData;
import com.wut.screencommonrx.Util.CollectionEmptyUtil;
import com.wut.screencommonrx.Util.DataParamParseUtil;
import com.wut.screencommonrx.Util.DateParamParseUtil;
import com.wut.screendbmysqlrx.Model.Posture;
import com.wut.screendbmysqlrx.Model.Traj;
import com.wut.screendbmysqlrx.Service.PostureService;
import com.wut.screendbmysqlrx.Util.DbModelTransformUtil;
import com.wut.screendbredisrx.Service.RedisTrajFusionService;
import com.wut.screenfusionrx.Util.TrafficModelParamUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import static com.wut.screencommonrx.Static.FusionModuleStatic.*;

@Component
public class PostureDataService {
    private final PostureService postureService;
    private final RedisTrajFusionService redisTrajFusionService;
    @Qualifier("fusionTaskAsyncPool")
    private final Executor fusionTaskAsyncPool;

    @Autowired
    public PostureDataService(PostureService postureService, RedisTrajFusionService redisTrajFusionService, Executor fusionTaskAsyncPool) {
        this.postureService = postureService;
        this.redisTrajFusionService = redisTrajFusionService;
        this.fusionTaskAsyncPool = fusionTaskAsyncPool;
    }

    public Posture collectPostureData(long timestamp) throws Exception {
        List<Traj> trajList = redisTrajFusionService.collectTrajData(timestamp - POSTURE_RECORD_TIME_COND + 1, timestamp).get(ASYNC_SERVICE_TIMEOUT, TimeUnit.SECONDS);
        Posture posture = DbModelTransformUtil.getPostureInstance(timestamp - POSTURE_RECORD_TIME_COND, timestamp);
        Map<Long, TrajRecordData> trajRecordDataMapToWH = new HashMap<>();
        Map<Long, TrajRecordData> trajRecordDataMapToEZ = new HashMap<>();
        List<Integer> carNumCountList = new ArrayList<>(Collections.nCopies(CAR_TYPE_COUNT * 2, 0));
        if (!CollectionEmptyUtil.forList(trajList)) {
            trajList.stream().forEach(traj -> {
                switch (traj.getRoadDirect()) {
                    case ROAD_DIRECT_TO_EZ -> TrafficModelParamUtil.recordTrajToMap(trajRecordDataMapToEZ, traj);
                    case ROAD_DIRECT_TO_WH -> TrafficModelParamUtil.recordTrajToMap(trajRecordDataMapToWH, traj);
                };
            });
        }
        recordCarNumCount(carNumCountList, trajRecordDataMapToWH, ROAD_DIRECT_TO_WH);
        recordCarNumCount(carNumCountList, trajRecordDataMapToEZ, ROAD_DIRECT_TO_EZ);
        posture.setComp(DataParamParseUtil.getPostureComp(carNumCountList));
        flushPostureData(posture, trajRecordDataMapToWH, trajRecordDataMapToEZ);
        return posture;
    }

    public void recordCarNumCount(List<Integer> carNumCountList, Map<Long, TrajRecordData> trajRecordDataMap, int direction) {
        trajRecordDataMap.entrySet().stream().forEach(record -> {
            int index = TrafficModelParamUtil.getCarNumCountIndex(direction, record.getValue().getType());
            carNumCountList.set(index, carNumCountList.get(index) + 1);
        });
    }

    public void flushPostureData(Posture posture, Map<Long, TrajRecordData> trajRecordDataMapToWH, Map<Long, TrajRecordData> trajRecordDataMapToEZ) {
        if (!CollectionEmptyUtil.forMap(trajRecordDataMapToWH)) {
            posture.setAvgQwh(TrafficModelParamUtil.getTrajRecordPostureArgQ(trajRecordDataMapToWH));
            posture.setAvgVwh(TrafficModelParamUtil.getTrajRecordArgV(trajRecordDataMapToWH));
            posture.setAvgKwh(SECTION_STREAM_SPEED / posture.getAvgVwh());
        }
        if (!CollectionEmptyUtil.forMap(trajRecordDataMapToEZ)) {
            posture.setAvgQez(TrafficModelParamUtil.getTrajRecordPostureArgQ(trajRecordDataMapToEZ));
            posture.setAvgVez(TrafficModelParamUtil.getTrajRecordArgV(trajRecordDataMapToEZ));
            posture.setAvgKez(SECTION_STREAM_SPEED / posture.getAvgVez());
        }
    }

    public CompletableFuture<Void> storePostureData(Posture posture, long timestamp) {
        return CompletableFuture.runAsync(() -> {
            postureService.storePostureData(DateParamParseUtil.getDateTimeStr(timestamp), posture);
        }, fusionTaskAsyncPool);
    }

}
