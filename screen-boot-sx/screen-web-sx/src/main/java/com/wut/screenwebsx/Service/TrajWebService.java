package com.wut.screenwebsx.Service;
import com.wut.screencommonsx.Entity.TrackDistinctEntity;
import com.wut.screencommonsx.Entity.TrackRecordEntity;
import com.wut.screencommonsx.Request.TrajTrackReq;
import com.wut.screencommonsx.Response.Track.TrajTrackDataResp;
import com.wut.screencommonsx.Response.Track.TrajTrackInfoData;
import com.wut.screencommonsx.Util.CollectionEmptyUtil;
import com.wut.screencommonsx.Util.DateParamParseUtil;
import com.wut.screendbmysqlsx.Model.Traj;
import com.wut.screendbmysqlsx.Service.TrajService;
import com.wut.screendbmysqlsx.Util.DbModelTransformUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import com.wut.screenwebsx.Config.DockingInterfaceConfig.*;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import static com.wut.screencommonsx.Static.WebModuleStatic.ASYNC_SERVICE_TIMEOUT;

@Component
public class TrajWebService {
    @Qualifier("webTaskAsyncPool")
    private final Executor webTaskAsyncPool;
    private final TrajService trajService;

    @Autowired
    public TrajWebService(Executor webTaskAsyncPool, TrajService trajService) {
        this.webTaskAsyncPool = webTaskAsyncPool;
        this.trajService = trajService;
    }

    @Docking
    public TrajTrackDataResp getTrajTrackData(TrajTrackReq req) {
        String tableDateStr = DateParamParseUtil.getDateTableStr(req.getTimestamp());
        List<TrackDistinctEntity> distinctList = initTrajDistinctCollection(tableDateStr);
        List<TrackRecordEntity> matchRecordList = recordMatchTrajId(distinctList, req.getLicense());
        Map<Long, List<Traj>> trajDataMap = initTrajDataCollection(tableDateStr, matchRecordList);
        return collectTrajTrackData(matchRecordList, trajDataMap);
    }

    public TrajTrackDataResp collectTrajTrackData(List<TrackRecordEntity> matchRecordList, Map<Long, List<Traj>> trajDataMap) {
        List<TrajTrackInfoData> trackInfoList = new ArrayList<>();
        if (CollectionEmptyUtil.forList(matchRecordList)) { return new TrajTrackDataResp(trackInfoList); }
        matchRecordList.stream().forEach(record -> {
            List<Traj> trajList = trajDataMap.get(record.getTrajId());
            TrajTrackInfoData trackInfoData = DbModelTransformUtil.trajAndMatchRecordToTrackInfoData(record, trajList.get(0), trajList.get(trajList.size() - 1));
            trackInfoData.getFrameList().addAll(trajList.stream().map(DbModelTransformUtil::trajToFrameData).toList());
            trackInfoList.add(trackInfoData);
        });
        return new TrajTrackDataResp(trackInfoList);
    }

    public List<TrackDistinctEntity> initTrajDistinctCollection(String date) {
        List<TrackDistinctEntity> distinctList = new ArrayList<>();
        try {
            List<Traj> data = CompletableFuture.supplyAsync(() -> {
                return trajService.getDistinctList(date);
            }, webTaskAsyncPool).get(ASYNC_SERVICE_TIMEOUT, TimeUnit.SECONDS);
            if (!CollectionEmptyUtil.forList(data)) {
                distinctList.addAll(data.stream().map(DbModelTransformUtil::trajToDistinctEntity).toList());
            }
        } catch (Exception e) { e.printStackTrace(); }
        return distinctList;
    }

    public Map<Long, List<Traj>> initTrajDataCollection(String date, List<TrackRecordEntity> recordList) {
        Map<Long, List<Traj>> trajDataMap = new HashMap<>();
        if (CollectionEmptyUtil.forList(recordList)) { return trajDataMap; }
        List<CompletableFuture<List<Traj>>> trajTaskList = recordList.stream().map(record -> {
            return CompletableFuture.supplyAsync(() -> trajService.getListByTrajId(date, record.getTrajId()), webTaskAsyncPool);
        }).toList();
        try {
            CompletableFuture.allOf(trajTaskList.toArray(CompletableFuture[]::new)).get(ASYNC_SERVICE_TIMEOUT, TimeUnit.SECONDS);
            trajTaskList.stream().map(completableFutureTask -> {
                try { return completableFutureTask.get(); }
                catch (Exception e) { return null; }
            }).filter(Objects::nonNull).forEach(trajList -> trajDataMap.put(trajList.get(0).getTrajId(), trajList));
        } catch (Exception e) { e.printStackTrace(); }
        return trajDataMap;
    }

    public List<TrackRecordEntity> recordMatchTrajId(List<TrackDistinctEntity> distinctList, String license) {
        List<TrackRecordEntity> recordList = new ArrayList<>();
        if (CollectionEmptyUtil.forList(distinctList)) { return recordList; }
        // 提取与输入车牌号右匹配的所有<轨迹号, 车牌号>
        // 每个有记录的轨迹号对应的匹配车牌号数量只可能是1(未绑定车牌)或2(已绑定车牌)
        Map<Long, List<String>> trajMatchMap = new HashMap<>();
        distinctList.stream().filter(entity -> entity.getCarId().matches("^" + license + ".*")).forEach(entity -> {
            if (!trajMatchMap.containsKey(entity.getTrajId())) {
                trajMatchMap.put(entity.getTrajId(), new ArrayList<>());
            }
            trajMatchMap.get(entity.getTrajId()).add(entity.getCarId());
        });
        if (CollectionEmptyUtil.forMap(trajMatchMap)) { return recordList; }
        // 提取每个有记录的轨迹号对应的所有车牌号
        // 对每个有记录的轨迹号,共维护两个列表: (1)轨迹号所有车牌号中右匹配的集合 (2)轨迹号所有车牌号的集合
        trajMatchMap.entrySet().stream().forEach(entry -> {
            List<String> licenseList = distinctList.stream().filter(entity -> Objects.equals(entry.getKey(), entity.getTrajId())).map(TrackDistinctEntity::getCarId).toList();
            recordList.add(filterMatchLicense(entry.getKey(), entry.getValue(), licenseList));
        });
        return recordList;
    }

    public TrackRecordEntity filterMatchLicense(Long trajId, List<String> matchList, List<String> licenseList) {
        TrackRecordEntity record = new TrackRecordEntity(trajId, null, null);
        record.setMatchName(matchList.size() < 2 ? matchList.get(0) : matchList.stream().filter(str -> !str.contains("*")).findAny().get());
        record.setFinalName(licenseList.size() < 2 ? licenseList.get(0) : licenseList.stream().filter(str -> !str.contains("*")).findAny().get());
        return record;
    }

}
