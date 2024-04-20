package com.wut.screenmsgtx.Service;

import com.wut.screencommontx.Entity.CollectDataParam;
import com.wut.screencommontx.Request.DateTimeOrderReq;
import com.wut.screencommontx.Util.CollectionSizeUtil;
import com.wut.screencommontx.Util.DateParamParseUtil;
import com.wut.screencommontx.Util.ModelTransformUtil;
import com.wut.screendbtx.Model.Fiber;
import com.wut.screendbtx.Model.Laser;
import com.wut.screendbtx.Model.Plate;
import com.wut.screendbtx.Model.Wave;
import com.wut.screenmsgtx.Context.MsgRedisDataContext;
import com.wut.screenmsgtx.Service.AsyncService.CollectDataAsyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static com.wut.screencommontx.Static.MsgModuleStatic.*;

@Component
public class CollectDataService {
    private final CollectDataAsyncService collectDataAsyncService;
    private final MsgRedisDataContext msgRedisDataContext;

    private static final int PARAM_PLATE_INDEX = 0;
    private static final int PARAM_FIBER_INDEX = 1;
    private static final int PARAM_LASER_INDEX = 2;
    private static final int PARAM_WAVE_INDEX = 3;

    @Autowired
    public CollectDataService(CollectDataAsyncService collectDataAsyncService, MsgRedisDataContext msgRedisDataContext) {
        this.collectDataAsyncService = collectDataAsyncService;
        this.msgRedisDataContext = msgRedisDataContext;
    }

    public void setInitParams(DateTimeOrderReq req) throws Exception{
        msgRedisDataContext.resetDataContext();
        if (req.getTime() == 0) {
            initTimestamp(req);
            msgRedisDataContext.setFindToday(req.getToday());
        } else {
            setTimestamp(DateParamParseUtil.getRoundTimestamp((double)req.getTime()));
            msgRedisDataContext.setFindToday(DateParamParseUtil.getDateTimeStr(req.getTime()));
            initOffset(req);
        }
    }

    public void setTimestamp(double timestamp) {
        msgRedisDataContext.setTimestamp(timestamp);
        msgRedisDataContext.setCollectTime(timestamp);
    }

    public void updateCollectTime() {
        msgRedisDataContext.updateCollectTime(COLLECT_COND_INTERVAL);
    }

    public void initTimestamp(DateTimeOrderReq req) throws Exception{
        CollectDataParam param = ModelTransformUtil.getCollectDataParamOnlyToday(req.getToday());
        var plateTask = collectDataAsyncService.collectPlateMinTimestamp(param);
        var fiberTask = collectDataAsyncService.collectFiberMinTimestamp(param);
        var laserTask = collectDataAsyncService.collectLaserMinTimestamp(param);
        var waveTask = collectDataAsyncService.collectWaveMinTimestamp(param);
        CompletableFuture.allOf(plateTask,fiberTask,laserTask,waveTask).get(ASYNC_SERVICE_TIMEOUT, TimeUnit.SECONDS);
        setTimestamp(DateParamParseUtil.getRoundTimestamp(Math.min(
            Math.min(fiberTask.get().getTimestamp(),plateTask.get().getGlobalTimestamp()),
            Math.min(laserTask.get().getTimestamp(),waveTask.get().getTimestamp())
        )));
    }

    public void initOffset(DateTimeOrderReq req) throws Exception{
        CollectDataParam param = ModelTransformUtil.getCollectDataParamNoOffset(DateParamParseUtil.getDateTimeStr(req.getTime()),req.getTime(),COLLECT_LIMIT);
        var plateTask = collectDataAsyncService.collectPlateFromTime(param);
        var fiberTask = collectDataAsyncService.collectFiberFromTime(param);
        var laserTask = collectDataAsyncService.collectLaserFromTime(param);
        var waveTask = collectDataAsyncService.collectWaveFromTime(param);
        CompletableFuture.allOf(plateTask,fiberTask,laserTask,waveTask).get(ASYNC_SERVICE_TIMEOUT,TimeUnit.SECONDS);
        msgRedisDataContext.updatePlateOffset(CollectionSizeUtil.forList(plateTask.get()));
        msgRedisDataContext.updateFiberOffset(CollectionSizeUtil.forList(fiberTask.get()));
        msgRedisDataContext.updateLaserOffset(CollectionSizeUtil.forList(laserTask.get()));
        msgRedisDataContext.updateWaveOffset(CollectionSizeUtil.forList(waveTask.get()));
    }

    public List<CollectDataParam> getCollectDataParams() {
        String collectToday = msgRedisDataContext.getFindToday();
        double collectTime = msgRedisDataContext.getCollectTime();
        return List.of(
            new CollectDataParam(collectToday,collectTime, COLLECT_LIMIT, msgRedisDataContext.getPlateOffset()),
            new CollectDataParam(collectToday,collectTime, COLLECT_LIMIT, msgRedisDataContext.getFiberOffset()),
            new CollectDataParam(collectToday,collectTime, COLLECT_LIMIT, msgRedisDataContext.getLaserOffset()),
            new CollectDataParam(collectToday,collectTime, COLLECT_LIMIT, msgRedisDataContext.getWaveOffset())
        );
    }

    public void collectDataAndUpdate() throws Exception{
        List<CollectDataParam> params = getCollectDataParams();
        var plateDbTask = collectDataAsyncService.collectPlateFromTime(params.get(PARAM_PLATE_INDEX));
        var fiberDbTask = collectDataAsyncService.collectFiberFromTime(params.get(PARAM_FIBER_INDEX));
        var laserDbTask = collectDataAsyncService.collectLaserFromTime(params.get(PARAM_LASER_INDEX));
        var waveDbTask = collectDataAsyncService.collectWaveFromTime(params.get(PARAM_WAVE_INDEX));
        CompletableFuture.allOf(plateDbTask,fiberDbTask,laserDbTask,waveDbTask).get(ASYNC_SERVICE_TIMEOUT,TimeUnit.SECONDS);
        updateCollectData(plateDbTask.get(),fiberDbTask.get(),laserDbTask.get(),waveDbTask.get());
    }

    public void updateCollectData(List<Plate> plateData,List<Fiber> fiberData,List<Laser> laserData,List<Wave> waveData) throws Exception{
        msgRedisDataContext.updatePlateOffset(CollectionSizeUtil.forList(plateData));
        msgRedisDataContext.updateFiberOffset(CollectionSizeUtil.forList(fiberData));
        msgRedisDataContext.updateLaserOffset(CollectionSizeUtil.forList(laserData));
        msgRedisDataContext.updateWaveOffset(CollectionSizeUtil.forList(waveData));
        var plateStoreTask = collectDataAsyncService.storeCollectPlateData(plateData);
        var fiberStoreTask = collectDataAsyncService.storeCollectFiberData(fiberData);
        var laserStoreTask = collectDataAsyncService.storeCollectLaserData(laserData);
        var waveStoreTask = collectDataAsyncService.storeCollectWaveData(waveData);
        CompletableFuture.allOf(plateStoreTask,fiberStoreTask,laserStoreTask,waveStoreTask).get(ASYNC_SERVICE_TIMEOUT,TimeUnit.SECONDS);
    }

}
