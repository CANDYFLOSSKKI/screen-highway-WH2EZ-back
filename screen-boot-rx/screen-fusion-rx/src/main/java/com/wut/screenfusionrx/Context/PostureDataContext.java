package com.wut.screenfusionrx.Context;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;

import static com.wut.screencommonrx.Static.FusionModuleStatic.FUSION_TIME_INTER;
import static com.wut.screencommonrx.Static.FusionModuleStatic.POSTURE_RECORD_TIME_COND;

@Component
public class PostureDataContext {
    @Qualifier("fusionTaskAsyncPool")
    private final Executor fusionTaskAsyncPool;
    @Getter
    private final AtomicLong timestamp = new AtomicLong(0L);

    @Autowired
    public PostureDataContext(Executor fusionTaskAsyncPool) {
        this.fusionTaskAsyncPool = fusionTaskAsyncPool;
    }

    public boolean recordTimestamp(long newTimestamp) {
        long oldTimestamp = timestamp.get();
        long timeInterval = newTimestamp - oldTimestamp;
        if (timeInterval == POSTURE_RECORD_TIME_COND) {
            timestamp.compareAndExchange(oldTimestamp, 0L);
            return true;
        }
        if (oldTimestamp == 0L || timeInterval < 0 || timeInterval > POSTURE_RECORD_TIME_COND) {
            timestamp.compareAndExchange(oldTimestamp, newTimestamp - FUSION_TIME_INTER);
        }
        return false;
    }

}
