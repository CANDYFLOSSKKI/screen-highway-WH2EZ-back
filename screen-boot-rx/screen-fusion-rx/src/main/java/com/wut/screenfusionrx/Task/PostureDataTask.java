package com.wut.screenfusionrx.Task;

import com.wut.screendbmysqlrx.Model.Posture;
import com.wut.screenfusionrx.Context.PostureDataContext;
import com.wut.screenfusionrx.Service.PostureDataService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class PostureDataTask {
    private final PostureDataService postureDataService;
    private final PostureDataContext postureDataContext;
    @Qualifier("fusionTaskAsyncPool")
    private final Executor fusionTaskAsyncPool;
    private static final ReentrantLock POSTURE_DATA_LOCK = new ReentrantLock();

    @Autowired
    public PostureDataTask(PostureDataContext postureDataContext, PostureDataService postureDataService, Executor fusionTaskAsyncPool) {
        this.postureDataContext = postureDataContext;
        this.postureDataService = postureDataService;
        this.fusionTaskAsyncPool = fusionTaskAsyncPool;
    }

    @RabbitListener(queues = "posture")
    public void postureDataListener(String timestampStr) {
        startParsePostureData(Long.parseLong(timestampStr)).thenRunAsync(() -> {});
    }

    public CompletableFuture<Void> startParsePostureData(long timestamp) {
        return CompletableFuture.runAsync(() -> {
            try {
                POSTURE_DATA_LOCK.lock();
                if (!postureDataContext.recordTimestamp(timestamp)) { return; }
                Posture posture = postureDataService.collectPostureData(timestamp);
                postureDataService.storePostureData(posture, timestamp).thenRunAsync(() -> {});
            } catch (Exception e) { e.printStackTrace(); }
            finally { POSTURE_DATA_LOCK.unlock(); }
        }, fusionTaskAsyncPool);
    }

}
