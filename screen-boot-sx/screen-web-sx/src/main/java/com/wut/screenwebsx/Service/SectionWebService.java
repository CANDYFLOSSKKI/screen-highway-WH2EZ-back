package com.wut.screenwebsx.Service;

import com.wut.screencommonsx.Response.PositionRecordData;
import com.wut.screencommonsx.Response.Section.*;
import com.wut.screencommonsx.Response.TimeRecordData;
import com.wut.screencommonsx.Util.CollectionEmptyUtil;
import com.wut.screencommonsx.Util.DateParamParseUtil;
import com.wut.screendbmysqlsx.Model.SecInfo;
import com.wut.screendbmysqlsx.Model.Section;
import com.wut.screendbmysqlsx.Service.SectionService;
import com.wut.screendbmysqlsx.Util.DbModelTransformUtil;
import com.wut.screenwebsx.Config.DockingInterfaceConfig.Docking;
import com.wut.screenwebsx.Context.SecInfoDataContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.wut.screencommonsx.Static.WebModuleStatic.ASYNC_SERVICE_TIMEOUT;

@Component
public class SectionWebService {
    @Qualifier("webTaskAsyncPool")
    private final Executor webTaskAsyncPool;
    private final SecInfoDataContext secInfoDataContext;
    private final SectionService sectionService;

    @Autowired
    public SectionWebService(Executor webTaskAsyncPool, SecInfoDataContext secInfoDataContext, SectionService sectionService) {
        this.webTaskAsyncPool = webTaskAsyncPool;
        this.secInfoDataContext = secInfoDataContext;
        this.sectionService = sectionService;
    }

    @Docking
    public SectionRealTimeDataResp getSectionRealTimeData(long timestamp) {
        String tableDateStr = DateParamParseUtil.getDateTableStr(timestamp);
        List<Section> sectionList = new ArrayList<>();
        try {
            List<Section> data = CompletableFuture.supplyAsync(() -> {
                return sectionService.getLatestList(tableDateStr);
            }, webTaskAsyncPool).get(ASYNC_SERVICE_TIMEOUT, TimeUnit.SECONDS);
            if (!CollectionEmptyUtil.forList(data)) {
                sectionList.addAll(data);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return collectSectionRealTimeData(sectionList);
    }

    @Docking
    public SectionPeriodDataResp getSectionPeriodData(long timestamp) {
        String tableDateStr = DateParamParseUtil.getDateTableStr(timestamp);
        List<Section> sectionList = new ArrayList<>();
        try {
            List<Section> data = CompletableFuture.supplyAsync(() -> {
                return sectionService.getListByDate(tableDateStr);
            }, webTaskAsyncPool).get(ASYNC_SERVICE_TIMEOUT, TimeUnit.SECONDS);
            if (!CollectionEmptyUtil.forList(data)) {
                sectionList.addAll(data);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return collectSectionPeriodData(sectionList);
    }

    @Docking
    public SecInfoDataResp getSecInfoData() {
        return new SecInfoDataResp(secInfoDataContext.getSecInfoList().stream().map(DbModelTransformUtil::secInfoToData).toList());
    }

    public SectionRealTimeDataResp collectSectionRealTimeData(List<Section> sectionList) {
        List<PositionRecordData> flowRecordList = initPositionRecordCollection();
        List<PositionRecordData> speedRecordList = initPositionRecordCollection();
        List<PositionRecordData> congestionRecordList = initPositionRecordCollection();
        if (CollectionEmptyUtil.forList(sectionList)) { return new SectionRealTimeDataResp(flowRecordList, speedRecordList, congestionRecordList); }
        AtomicInteger index = new AtomicInteger(0);
        sectionList.stream().forEach(section -> {
            recordPositionData(flowRecordList.get(index.get()), section.getAvgQez(), section.getAvgQwh());
            recordPositionData(speedRecordList.get(index.get()), section.getAvgVez(), section.getAvgVwh());
            recordPositionData(congestionRecordList.get(index.get()), section.getAvgKez(), section.getAvgKwh());
           index.incrementAndGet();
        });
        return new SectionRealTimeDataResp(flowRecordList, speedRecordList, congestionRecordList);
    }

    public SectionPeriodDataResp collectSectionPeriodData(List<Section> sectionList) {
        List<SectionTimeData> timeDataList = initTimeDataCollection();
        if (CollectionEmptyUtil.forList(sectionList)) { return new SectionPeriodDataResp(timeDataList); }
        List<Section> sortedSectionList = sectionList.stream().sorted(Comparator.comparing(Section::getTimestampStart).thenComparing(Section::getXsecValue)).toList();
        timeDataList.stream().forEach(timeData -> {
            sortedSectionList.stream().filter(section -> section.getXsecValue() == timeData.getXsecValue())
                    .sorted(Comparator.comparingLong(Section::getTimestampEnd)).forEach(section -> {
                        recordTimeData(timeData.getFlowRecordList(), section.getTimestampEnd(), section.getAvgQez(), section.getAvgQwh());
                        recordTimeData(timeData.getSpeedRecordList(), section.getTimestampEnd(), section.getAvgVez(), section.getAvgVwh());
                        recordTimeData(timeData.getCongestionRecordList(), section.getTimestampEnd(), section.getAvgKez(), section.getAvgKwh());
                    });
        });
        return new SectionPeriodDataResp(timeDataList);
    }

    public List<PositionRecordData> initPositionRecordCollection() {
        List<SecInfo> secInfoList = secInfoDataContext.getSecInfoList();
        return secInfoList.stream().map(DbModelTransformUtil::secInfoToPositionRecordData).toList();
    }

    public List<SectionTimeData> initTimeDataCollection() {
        List<SecInfo> secInfoList = secInfoDataContext.getSecInfoList();
        return secInfoList.stream().map(DbModelTransformUtil::secInfoToSectionTimeData).toList();
    }

    public void recordPositionData(PositionRecordData record, double valueToEZ, double valueToWH) {
        record.setValueToEZ(valueToEZ);
        record.setValueToWH(valueToWH);
    }

    public void recordTimeData(List<TimeRecordData> recordList, long timestamp, double valueToEZ, double valueToWH) {
        String timeStr = DateParamParseUtil.getTimeDataStr(timestamp);
        recordList.add(new TimeRecordData(timeStr, timestamp, valueToEZ, valueToWH));
    }

}
