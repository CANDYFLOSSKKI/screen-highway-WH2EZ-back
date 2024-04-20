package com.wut.screenmsgtx.Service;

import com.wut.screendbtx.Model.Fiber;
import com.wut.screendbtx.Model.Laser;
import com.wut.screendbtx.Model.Plate;
import com.wut.screendbtx.Model.Wave;
import com.wut.screenmsgtx.Context.MsgRedisDataContext;
import com.wut.screenmsgtx.Service.AsyncService.TransmitDataAsyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static com.wut.screencommontx.Static.MsgModuleStatic.ASYNC_SERVICE_TIMEOUT;
import static com.wut.screencommontx.Static.MsgModuleStatic.TRANSMIT_COND_INTERVAL;

@Component
public class TransmitDataService {
    private final TransmitDataAsyncService transmitDataAsyncService;
    private final MsgRedisDataContext msgRedisDataContext;

    @Autowired
    public TransmitDataService(TransmitDataAsyncService transmitDataAsyncService, MsgRedisDataContext msgRedisDataContext) {
        this.transmitDataAsyncService = transmitDataAsyncService;
        this.msgRedisDataContext = msgRedisDataContext;
    }

    public void updateTimestamp() {
        msgRedisDataContext.updateTimestamp(TRANSMIT_COND_INTERVAL);
    }

    public void filterDataAndSend() throws Exception{
        double timestamp = msgRedisDataContext.getTimestamp();
        var plateFilterTask = transmitDataAsyncService.filterCollectPlateData(timestamp);
        var fiberFilterTask = transmitDataAsyncService.filterCollectFiberData(timestamp);
        var laserFilterTask = transmitDataAsyncService.filterCollectLaserData(timestamp);
        var waveFilterTask = transmitDataAsyncService.filterCollectWaveData(timestamp);
        CompletableFuture.allOf(plateFilterTask,fiberFilterTask,laserFilterTask,waveFilterTask).get(ASYNC_SERVICE_TIMEOUT, TimeUnit.SECONDS);
        sendFilterData(plateFilterTask.get(),fiberFilterTask.get(),laserFilterTask.get(),waveFilterTask.get(),timestamp);
    }

    public void sendFilterData(List<Plate> plateData, List<Fiber> fiberData, List<Laser> laserData, List<Wave> waveData, double timestamp) throws Exception{
        var plateSendTask = transmitDataAsyncService.transmitPlateData(plateData,timestamp);
        var fiberSendTask = transmitDataAsyncService.transmitFiberData(fiberData,timestamp);
        var laserSendTask = transmitDataAsyncService.transmitLaserData(laserData,timestamp);
        var waveSendTask = transmitDataAsyncService.transmitWaveData(waveData,timestamp);
        CompletableFuture.allOf(plateSendTask,fiberSendTask,laserSendTask,waveSendTask).get(ASYNC_SERVICE_TIMEOUT,TimeUnit.SECONDS);
        transmitDataAsyncService.transmitTimestamp(timestamp).thenRunAsync(() -> {});
    }

}
