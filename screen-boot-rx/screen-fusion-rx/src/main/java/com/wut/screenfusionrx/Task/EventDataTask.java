package com.wut.screenfusionrx.Task;

import com.wut.screencommonrx.Model.CarEventModel;
import com.wut.screenfusionrx.Service.EventDataService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class EventDataTask {
    private final EventDataService eventDataService;
    @Qualifier("fusionTaskAsyncPool")
    private final Executor fusionTaskAsyncPool;
    private static final ReentrantLock EVENT_DATA_LOCK = new ReentrantLock();

    @Autowired
    public EventDataTask(EventDataService eventDataService, Executor fusionTaskAsyncPool) {
        this.eventDataService = eventDataService;
        this.fusionTaskAsyncPool = fusionTaskAsyncPool;
    }

    @RabbitListener(queues = "event")
    public void eventDataListener(String timestampStr) {
        startParseEventData(Long.parseLong(timestampStr)).thenRunAsync(() -> {});
    }

    public CompletableFuture<Void> startParseEventData(long timestamp) {
        return CompletableFuture.runAsync(() -> {
            try {
                EVENT_DATA_LOCK.lock();
                Map<String, CarEventModel> eventModelMap = eventDataService.collectEventModelData(timestamp);
                eventDataService.storeEventModelData(eventModelMap, timestamp);
            } catch (Exception e) { e.printStackTrace(); }
            finally { EVENT_DATA_LOCK.unlock(); }
        },fusionTaskAsyncPool);
    }

}
