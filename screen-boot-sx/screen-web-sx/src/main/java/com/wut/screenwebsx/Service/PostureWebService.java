package com.wut.screenwebsx.Service;

import com.wut.screencommonsx.Response.Posture.PostureFlowTypeData;
import com.wut.screencommonsx.Response.Posture.PosturePeriodDataResp;
import com.wut.screencommonsx.Response.Posture.PostureRealTimeDataResp;
import com.wut.screencommonsx.Response.Posture.PostureStatisticData;
import com.wut.screencommonsx.Response.TimeRecordData;
import com.wut.screencommonsx.Util.CollectionEmptyUtil;
import com.wut.screencommonsx.Util.DataParamParseUtil;
import com.wut.screencommonsx.Util.DateParamParseUtil;
import com.wut.screencommonsx.Util.ModelTransformUtil;
import com.wut.screendbmysqlsx.Model.Posture;
import com.wut.screendbmysqlsx.Service.PostureService;
import com.wut.screenwebsx.Config.DockingInterfaceConfig.Docking;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static com.wut.screencommonsx.Static.WebModuleStatic.*;

@Component
public class PostureWebService {
    @Qualifier("webTaskAsyncPool")
    private final Executor webTaskAsyncPool;
    private final PostureService postureService;

    @Autowired
    public PostureWebService(Executor webTaskAsyncPool, PostureService postureService) {
        this.webTaskAsyncPool = webTaskAsyncPool;
        this.postureService = postureService;
    }

    @Docking
    public PostureRealTimeDataResp getRealTimePostureData(long timestamp) {
        String tableDateStr = DateParamParseUtil.getDateTableStr(timestamp);
        Posture posture = null;
        try {
            posture = CompletableFuture.supplyAsync(() -> {
                return postureService.getLatestOne(tableDateStr);
            }, webTaskAsyncPool).get(ASYNC_SERVICE_TIMEOUT, TimeUnit.SECONDS);
        } catch (Exception e) { e.printStackTrace(); }
        return collectPostureRealTimeData(posture);
    }

    @Docking
    public PosturePeriodDataResp getPeriodPostureData(long timestamp) {
        String tableDateStr = DateParamParseUtil.getDateTableStr(timestamp);
        List<Posture> postureList = new ArrayList<>();
        try {
            List<Posture> data = CompletableFuture.supplyAsync(() -> {
                return postureService.getListByDate(tableDateStr);
            }, webTaskAsyncPool).get(ASYNC_SERVICE_TIMEOUT, TimeUnit.SECONDS);
            if (!CollectionEmptyUtil.forList(data)) {
                postureList.addAll(data);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return collectPosturePeriodData(postureList);
    }

    public PostureRealTimeDataResp collectPostureRealTimeData(Posture posture) {
        PostureStatisticData statisticData = ModelTransformUtil.getPostureStatisticInstance();
        List<PostureFlowTypeData> flowTypeList = initFlowTypeCollection();
        if (posture == null) { return new PostureRealTimeDataResp(statisticData, flowTypeList); }
        recordStatisticData(statisticData, posture);
        recordFlowType(flowTypeList, DataParamParseUtil.parsePostureComp(posture.getComp()));
        return new PostureRealTimeDataResp(statisticData, flowTypeList);
    }

    public PosturePeriodDataResp collectPosturePeriodData(List<Posture> postureList) {
        List<TimeRecordData> flowRecordList = new ArrayList<>();
        List<TimeRecordData> speedRecordList = new ArrayList<>();
        List<TimeRecordData> congestionRecordList = new ArrayList<>();
        if (CollectionEmptyUtil.forList(postureList)) { return new PosturePeriodDataResp(flowRecordList, speedRecordList, congestionRecordList); }
        postureList.stream().sorted(Comparator.comparingDouble(Posture::getTimestampStart)).forEach(posture -> {
            String timeStr = DateParamParseUtil.getTimeDataStr(posture.getTimestampEnd());
            flowRecordList.add(new TimeRecordData(timeStr, posture.getTimestampEnd(), posture.getAvgQez(), posture.getAvgQwh()));
            speedRecordList.add(new TimeRecordData(timeStr, posture.getTimestampEnd(), posture.getAvgVez(), posture.getAvgVwh()));
            congestionRecordList.add(new TimeRecordData(timeStr, posture.getTimestampEnd(), posture.getAvgKez(), posture.getAvgKwh()));
        });
        return new PosturePeriodDataResp(flowRecordList, speedRecordList, congestionRecordList);
    }

    public List<PostureFlowTypeData> initFlowTypeCollection() {
        return Stream.of(
                    CAR_TYPE_COMPACT,
                    CAR_TYPE_TRUCK,
                    CAR_TYPE_BUS,
                    CAR_TYPE_SPECIAL
        ).map(ModelTransformUtil::carTypeToPostureFlowTypeData).toList();
    }

    public void recordStatisticData(PostureStatisticData statisticData, Posture posture) {
        statisticData.setFlowToEZ(posture.getAvgQez());
        statisticData.setFlowToWH(posture.getAvgQwh());
        statisticData.setSpeedToEZ(posture.getAvgVez());
        statisticData.setSpeedToWH(posture.getAvgVwh());
        statisticData.setCongestionToEZ(posture.getAvgKez());
        statisticData.setCongestionToWH(posture.getAvgKwh());
    }

    public void recordFlowType(List<PostureFlowTypeData> flowTypeList, List<Integer> compList) {
        flowTypeList.stream().forEach(flowType -> {
            int indexToEZ = DataParamParseUtil.getCarNumCountIndex(TRAJ_ROAD_DIRECT_TO_EZ, flowType.getType());
            int indexToWH = DataParamParseUtil.getCarNumCountIndex(TRAJ_ROAD_DIRECT_TO_WH, flowType.getType());
            flowType.setCarNumToEZ(compList.get(indexToEZ));
            flowType.setCarNumToWH(compList.get(indexToWH));
            flowType.setFlowToEZ(compList.get(indexToEZ) * 60);
            flowType.setFlowToWH(compList.get(indexToWH) * 60);
        });
    }

}
