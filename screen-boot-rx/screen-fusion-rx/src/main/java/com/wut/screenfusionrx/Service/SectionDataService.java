package com.wut.screenfusionrx.Service;

import com.wut.screencommonrx.Util.CollectionEmptyUtil;
import com.wut.screencommonrx.Util.DataParamParseUtil;
import com.wut.screencommonrx.Util.DateParamParseUtil;
import com.wut.screencommonrx.Util.MessagePrintUtil;
import com.wut.screendbmysqlrx.Model.SecInfo;
import com.wut.screendbmysqlrx.Model.Section;
import com.wut.screendbmysqlrx.Model.Traj;
import com.wut.screendbmysqlrx.Service.SectionService;
import com.wut.screendbmysqlrx.Util.DbModelTransformUtil;
import com.wut.screendbredisrx.Service.RedisTrajFusionService;
import com.wut.screenfusionrx.Context.SectionDataContext;
import com.wut.screenfusionrx.Model.SecRecordModel;
import com.wut.screenfusionrx.Util.TrafficModelParamUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import static com.wut.screencommonrx.Static.FusionModuleStatic.*;

@Component
public class SectionDataService {
    private final SectionService sectionService;
    private final SectionDataContext sectionDataContext;
    private final RedisTrajFusionService redisTrajFusionService;
    @Qualifier("fusionTaskAsyncPool")
    private final Executor fusionTaskAsyncPool;

    @Autowired
    public SectionDataService(RedisTrajFusionService redisTrajFusionService, Executor fusionTaskAsyncPool, SectionService sectionService, SectionDataContext sectionDataContext) {
        this.redisTrajFusionService = redisTrajFusionService;
        this.fusionTaskAsyncPool = fusionTaskAsyncPool;
        this.sectionService = sectionService;
        this.sectionDataContext = sectionDataContext;
    }

    public List<Section> collectSectionData(long timestamp) throws Exception {
        List<Traj> trajList = redisTrajFusionService.collectTrajData(timestamp - SECTION_RECORD_TIME_COND + 1, timestamp).get(ASYNC_SERVICE_TIMEOUT, TimeUnit.SECONDS);
        List<SecRecordModel> secRecordModelList = initSectionCollection(timestamp - SECTION_RECORD_TIME_COND, timestamp);
        if (!CollectionEmptyUtil.forList(trajList)) {
            trajList.stream().forEach(traj -> recordTrajToSection(secRecordModelList, traj));
        }
        return flushSectionData(secRecordModelList);
    }

    public List<SecRecordModel> initSectionCollection(long timeStart, long timeEnd) {
        List<SecInfo> secInfoList = sectionDataContext.getSecInfoList();
        return secInfoList.stream().map(secInfo -> new SecRecordModel(DbModelTransformUtil.infoToSection(secInfo, timeStart, timeEnd), new HashMap<>(), new HashMap<>())).toList();
    }

    public void recordTrajToSection(List<SecRecordModel> secRecordModelList, Traj traj) {
        secRecordModelList.stream().filter(record -> {
            double position = record.getSection().getXsecValue();
            return DataParamParseUtil.isBetweenRamp(traj.getFrenetX(), position, position);
        }).findFirst().ifPresent(record -> {
            switch (traj.getRoadDirect()) {
                case ROAD_DIRECT_TO_EZ -> TrafficModelParamUtil.recordTrajToMap(record.getTrajRecordMapToEZ(), traj);
                case ROAD_DIRECT_TO_WH -> TrafficModelParamUtil.recordTrajToMap(record.getTrajRecordMapToWH(), traj);
            }
        });
    }

    public List<Section> flushSectionData(List<SecRecordModel> secRecordModelList) {
        return secRecordModelList.stream().map(secRecordModel -> {
            Section section = secRecordModel.getSection();
            if (!CollectionEmptyUtil.forMap(secRecordModel.getTrajRecordMapToEZ())) {
                section.setAvgQez(TrafficModelParamUtil.getTrajRecordSectionArgQ(secRecordModel.getTrajRecordMapToEZ()));
                section.setAvgVez(TrafficModelParamUtil.getTrajRecordArgV(secRecordModel.getTrajRecordMapToEZ()));
                section.setAvgKez(SECTION_STREAM_SPEED / section.getAvgVez());
            }
            if (!CollectionEmptyUtil.forMap(secRecordModel.getTrajRecordMapToWH())) {
                section.setAvgQwh(TrafficModelParamUtil.getTrajRecordSectionArgQ(secRecordModel.getTrajRecordMapToWH()));
                section.setAvgVwh(TrafficModelParamUtil.getTrajRecordArgV(secRecordModel.getTrajRecordMapToWH()));
                section.setAvgKwh(SECTION_STREAM_SPEED / section.getAvgVwh());
            }
            return section;
        }).toList();
    }

    public CompletableFuture<Void> storeSectionData(List<Section> sectionList, long timestamp) {
        return CompletableFuture.runAsync(() -> {
            sectionService.storeSectionData(DateParamParseUtil.getDateTimeStr(timestamp), sectionList);
            MessagePrintUtil.printSectionData(timestamp);
        }, fusionTaskAsyncPool);
    }

}
