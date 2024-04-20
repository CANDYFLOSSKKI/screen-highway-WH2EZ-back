package com.wut.screenwebsx.Context;

import com.wut.screendbmysqlsx.Model.SecInfo;
import com.wut.screendbmysqlsx.Service.SecInfoService;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Component
public class SecInfoDataContext {
    @Qualifier("webTaskAsyncPool")
    private final Executor webTaskAsyncPool;
    private final SecInfoService secInfoService;
    @Getter
    private final List<SecInfo> secInfoList = new ArrayList<>();

    @Autowired
    public SecInfoDataContext(Executor webTaskAsyncPool, SecInfoService secInfoService) {
        this.webTaskAsyncPool = webTaskAsyncPool;
        this.secInfoService = secInfoService;
    }

    @PostConstruct
    public void initSecInfoList() {
        updateSecInfoList().thenRunAsync(() -> {});
    }

    public CompletableFuture<Void> updateSecInfoList() {
        return CompletableFuture.runAsync(() -> {
            secInfoList.clear();
            secInfoList.addAll(secInfoService.getAllSecInfo());
        }, webTaskAsyncPool);
    }

}
