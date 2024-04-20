package com.wut.screendbredisrx.Context;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;

import static com.wut.screencommonrx.Static.FusionModuleStatic.BATCH_RECORD_TIME_COND;
import static com.wut.screencommonrx.Static.FusionModuleStatic.FUSION_TIME_INTER;

@Component
public class BatchCacheContext {
    @Qualifier("redisTaskAsyncPool")
    private final Executor redisTaskAsyncPool;
    @Getter
    private final AtomicLong trajTimestamp = new AtomicLong(0L);
    @Getter
    private final AtomicLong eventTimestamp = new AtomicLong(0L);

    @Autowired
    public BatchCacheContext(Executor redisTaskAsyncPool) {
        this.redisTaskAsyncPool = redisTaskAsyncPool;
    }

    public boolean recordTrajCacheTimestamp(long newTimestamp) {
        long oldTimestamp = trajTimestamp.get();
        long timeInterval = newTimestamp - oldTimestamp;
        if (timeInterval == BATCH_RECORD_TIME_COND) {
            trajTimestamp.compareAndExchange(oldTimestamp, 0L);
            return true;
        }
        if (oldTimestamp == 0L || timeInterval < 0 || timeInterval > BATCH_RECORD_TIME_COND) {
            trajTimestamp.compareAndExchange(oldTimestamp, newTimestamp - FUSION_TIME_INTER);
        }
        return false;
    }

    public boolean recordEventCacheTimestamp(long newTimestamp) {
        long oldTimestamp = eventTimestamp.get();
        long timeInterval = newTimestamp - oldTimestamp;
        if (timeInterval == BATCH_RECORD_TIME_COND) {
            eventTimestamp.compareAndExchange(oldTimestamp, 0L);
            return true;
        }
        if (oldTimestamp == 0L || timeInterval < 0 || timeInterval > BATCH_RECORD_TIME_COND) {
            eventTimestamp.compareAndExchange(oldTimestamp, newTimestamp - FUSION_TIME_INTER);
        }
        return false;
    }

}
