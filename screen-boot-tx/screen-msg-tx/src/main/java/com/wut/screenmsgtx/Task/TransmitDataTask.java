package com.wut.screenmsgtx.Task;

import com.wut.screenmsgtx.Service.TransmitDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledFuture;

import static com.wut.screencommontx.Static.MsgModuleStatic.TRANSMIT_START_TIMEOUT;
import static com.wut.screencommontx.Static.MsgModuleStatic.TRANSMIT_TASK_TIME_RATE;

@Component
public class TransmitDataTask {
    @Qualifier("transmitDataTaskScheduler")
    private final ThreadPoolTaskScheduler transmitDataTaskScheduler;
    private final TransmitDataService transmitDataService;
    private final KafkaTemplate kafkaTemplate;
    private ScheduledFuture future;

    @Autowired
    public TransmitDataTask(ThreadPoolTaskScheduler transmitDataTaskScheduler, TransmitDataService transmitDataService, KafkaTemplate kafkaTemplate) {
        this.transmitDataTaskScheduler = transmitDataTaskScheduler;
        this.transmitDataService = transmitDataService;
        this.kafkaTemplate = kafkaTemplate;
    }

    public void startTask() {
        try {
            Thread.sleep(TRANSMIT_START_TIMEOUT);
            future = transmitDataTaskScheduler.scheduleAtFixedRate(this::transmitData, Duration.ofMillis(TRANSMIT_TASK_TIME_RATE));
        } catch (Exception ignored) {}
    }

    public void endTask() {
        if (future != null) {
            future.cancel(false);
            kafkaTemplate.flush();
        }
    }

    public void transmitData(){
        CompletableFuture.runAsync(() -> {
            try {
                transmitDataService.updateTimestamp();
                transmitDataService.filterDataAndSend();
            } catch (Exception e) { e.printStackTrace(); }
        },transmitDataTaskScheduler);
    }

}
