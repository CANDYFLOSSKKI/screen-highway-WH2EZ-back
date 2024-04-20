package com.wut.screenmsgrx.Service;

import com.wut.screencommonrx.Util.DateParamParseUtil;
import com.wut.screencommonrx.Util.MessagePrintUtil;
import com.wut.screendbredisrx.Service.RedisDateTimeService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import static com.wut.screencommonrx.Static.FusionModuleStatic.ASYNC_SERVICE_TIMEOUT;
import static com.wut.screencommonrx.Static.MsgModuleStatic.*;

@Component
public class DateTimeParseService {
    private final DynamicInitService dynamicInitService;
    private final RedisDateTimeService redisDateTimeService;
    private final RabbitTemplate rabbitTemplate;
    @Qualifier("msgTaskAsyncPool")
    private final Executor msgTaskAsyncPool;

    @Autowired
    public DateTimeParseService(DynamicInitService dynamicInitService, RedisDateTimeService redisDateTimeService, RabbitTemplate rabbitTemplate, Executor msgTaskAsyncPool) {
        this.dynamicInitService = dynamicInitService;
        this.redisDateTimeService = redisDateTimeService;
        this.rabbitTemplate = rabbitTemplate;
        this.msgTaskAsyncPool = msgTaskAsyncPool;
    }

    public void collectTimestampData(String timestampStr) {
        try {
            String datetimeStr = DateParamParseUtil.getDateTimeStr(Long.parseLong(timestampStr));
            checkDbTableState(datetimeStr);
            sendTimestampMsg(timestampStr).thenRunAsync(() -> {});
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void checkDbTableState(String datetimeStr) throws Exception{
        String recordDatetimeStr = redisDateTimeService.getRecordDateTime();
        if (recordDatetimeStr != null && Objects.equals(recordDatetimeStr, datetimeStr)) { return; }
        redisDateTimeService.setRecordDateTime(datetimeStr);
        dynamicInitService.initDynamicTable(datetimeStr).get(ASYNC_SERVICE_TIMEOUT, TimeUnit.SECONDS);
        MessagePrintUtil.printDbState(datetimeStr);
    }

    public CompletableFuture<Void> sendTimestampMsg(String timestampStr){
        return CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(MODEL_FLUSH_WAIT_TIME);
                rabbitTemplate.convertAndSend(QUEUE_DEFAULT_EXCHANGE, QUEUE_NAME_FLUSH, timestampStr);
            } catch (Exception e) { e.printStackTrace(); }
        }, msgTaskAsyncPool);
    }

}
