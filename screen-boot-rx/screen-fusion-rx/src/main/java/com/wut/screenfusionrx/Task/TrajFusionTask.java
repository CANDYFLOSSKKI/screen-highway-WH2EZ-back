package com.wut.screenfusionrx.Task;

import com.wut.screendbmysqlrx.Model.Traj;
import com.wut.screenfusionrx.Service.TrajFusionService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class TrajFusionTask {
    private final TrajFusionService trajFusionService;
    @Qualifier("fusionTaskAsyncPool")
    private final Executor fusionTaskAsyncPool;
    private static final ReentrantLock TRAJ_FUSION_LOCK = new ReentrantLock();

    @Autowired
    public TrajFusionTask(TrajFusionService trajFusionService, Executor fusionTaskAsyncPool) {
        this.trajFusionService = trajFusionService;
        this.fusionTaskAsyncPool = fusionTaskAsyncPool;
    }

    @RabbitListener(queues = "fusion")
    public void trajFusionListener(String timestampStr) {
        startTrajFusion(Long.parseLong(timestampStr)).thenRunAsync(() -> {});
    }

    public CompletableFuture<Void> startTrajFusion(long timestamp) {
        return CompletableFuture.runAsync(() -> {
            try {
                TRAJ_FUSION_LOCK.lock();
                List<Traj> trajList = trajFusionService.collectFusionTraj(timestamp);
                trajFusionService.storeTrajListData(trajList, timestamp).thenRunAsync(() -> {});
                trajFusionService.sendTrajListData(trajList, timestamp).thenRunAsync(() -> {});
            } catch (Exception e) { e.printStackTrace(); }
            finally { TRAJ_FUSION_LOCK.unlock(); }
        }, fusionTaskAsyncPool);
    }

}
