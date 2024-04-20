package com.wut.screenmsgtx.Task;

import com.wut.screencommontx.Request.DateTimeOrderReq;
import com.wut.screenmsgtx.Service.CollectDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledFuture;

import static com.wut.screencommontx.Static.MsgModuleStatic.COLLECT_TASK_TIME_CRON;

@Component
public class CollectDataTask {
    @Qualifier("collectDataTaskScheduler")
    private final ThreadPoolTaskScheduler collectDataTaskScheduler;
    private final CollectDataService collectDataService;
    private ScheduledFuture future;

    @Autowired
    public CollectDataTask(ThreadPoolTaskScheduler collectDataTaskScheduler, CollectDataService collectDataService) {
        this.collectDataTaskScheduler = collectDataTaskScheduler;
        this.collectDataService = collectDataService;
    }

    public void startTask(DateTimeOrderReq req) {
        try {
            collectDataService.setInitParams(req);
            future = collectDataTaskScheduler.schedule(this::collectDataFromTime, new CronTrigger(COLLECT_TASK_TIME_CRON));
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void endTask() {
        if (future != null) {
            future.cancel(false);
        }
    }

    public void collectDataFromTime() {
        CompletableFuture.runAsync(() -> {
            try {
                collectDataService.updateCollectTime();
                collectDataService.collectDataAndUpdate();
            } catch (Exception e) { e.printStackTrace(); }
        },collectDataTaskScheduler);
    }

}
