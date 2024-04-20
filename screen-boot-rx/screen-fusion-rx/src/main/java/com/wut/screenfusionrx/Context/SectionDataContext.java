package com.wut.screenfusionrx.Context;

import com.wut.screendbmysqlrx.Model.SecInfo;
import com.wut.screendbmysqlrx.Service.SecInfoService;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;

import static com.wut.screencommonrx.Static.FusionModuleStatic.FUSION_TIME_INTER;
import static com.wut.screencommonrx.Static.FusionModuleStatic.SECTION_RECORD_TIME_COND;

@Component
public class SectionDataContext {
    private final SecInfoService secInfoService;
    @Qualifier("fusionTaskAsyncPool")
    private final Executor fusionTaskAsyncPool;
    @Getter
    private final List<SecInfo> secInfoList = new ArrayList<>();
    private final AtomicLong timestamp = new AtomicLong(0L);

    @Autowired
    public SectionDataContext(SecInfoService secInfoService, Executor fusionTaskAsyncPool) {
        this.secInfoService = secInfoService;
        this.fusionTaskAsyncPool = fusionTaskAsyncPool;
    }

    @PostConstruct
    public void initSecInfoList() {
        updateSecInfoList().thenRunAsync(() -> {});
    }

    public CompletableFuture<Void> updateSecInfoList() {
        return CompletableFuture.runAsync(() -> {
            secInfoList.clear();
            secInfoList.addAll(secInfoService.getAllSecInfo());
        }, fusionTaskAsyncPool);
    }

    public boolean recordTimestamp(long newTimestamp) {
        long oldTimestamp = timestamp.get();
        long timeInterval = newTimestamp - oldTimestamp;
        if (timeInterval == SECTION_RECORD_TIME_COND) {
            timestamp.compareAndExchange(oldTimestamp, 0L);
            return true;
        }
        if (oldTimestamp == 0L || timeInterval < 0 || timeInterval > SECTION_RECORD_TIME_COND) {
            timestamp.compareAndExchange(oldTimestamp, newTimestamp - FUSION_TIME_INTER);
        }
        return false;
    }

}
