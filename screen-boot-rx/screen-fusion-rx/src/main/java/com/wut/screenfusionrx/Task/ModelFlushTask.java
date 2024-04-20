package com.wut.screenfusionrx.Task;

import com.wut.screencommonrx.Model.VehicleModel;
import com.wut.screenfusionrx.Service.ModelFlushService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class ModelFlushTask {
    private final ModelFlushService modelFlushService;
    @Qualifier("fusionTaskAsyncPool")
    private final Executor fusionTaskAsyncPool;
    private static final ReentrantLock MODEL_FLUSH_LOCK = new ReentrantLock();

    @Autowired
    public ModelFlushTask(ModelFlushService modelFlushService, Executor fusionTaskAsyncPool) {
        this.modelFlushService = modelFlushService;
        this.fusionTaskAsyncPool = fusionTaskAsyncPool;
    }

    @RabbitListener(queues = "flush")
    public void modelFlushListener(String timestampStr) {
        startModelFlush(Long.parseLong(timestampStr)).thenRunAsync(() -> {});
    }

    public CompletableFuture<Void> startModelFlush(long timestamp){
        return CompletableFuture.runAsync(() -> {
            try {
                MODEL_FLUSH_LOCK.lock();
                List<VehicleModel> targetModelList = modelFlushService.collectTargetModel(timestamp);
                modelFlushService.storeFlushModel(targetModelList, timestamp).thenRunAsync(() -> {});
            } catch (Exception e) { e.printStackTrace(); }
            finally { MODEL_FLUSH_LOCK.unlock(); }
        },fusionTaskAsyncPool);
    }

}
