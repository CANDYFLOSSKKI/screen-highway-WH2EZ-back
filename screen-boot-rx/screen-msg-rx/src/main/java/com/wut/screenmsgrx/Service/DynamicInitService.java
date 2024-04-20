package com.wut.screenmsgrx.Service;

import com.wut.screendbmysqlrx.Service.*;
import com.wut.screendbredisrx.Service.RedisTrajFusionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Component
public class DynamicInitService {
    @Qualifier("msgTaskAsyncPool")
    private final Executor msgTaskAsyncPool;
    private final RedisTrajFusionService redisTrajFusionService;
    private final TrajCarPlateService trajCarPlateService;
    private final TrajService trajService;
    private final SectionService sectionService;
    private final CarEventService carEventService;
    private final PostureService postureService;

    @Autowired
    public DynamicInitService(RedisTrajFusionService redisTrajFusionService, Executor msgTaskAsyncPool, TrajCarPlateService trajCarPlateService, TrajService trajService, SectionService sectionService, CarEventService carEventService, PostureService postureService) {
        this.msgTaskAsyncPool = msgTaskAsyncPool;
        this.redisTrajFusionService = redisTrajFusionService;
        this.trajCarPlateService = trajCarPlateService;
        this.trajService = trajService;
        this.sectionService = sectionService;
        this.carEventService = carEventService;
        this.postureService = postureService;
    }

    public CompletableFuture<Void> initDynamicTable(String time) {
        var trajTableTask = initTrajTable(time);
        var sectionTableTask = initSectionTable(time);
        var eventTableTask = initEventTable(time);
        var postureTableTask = initPostureTable(time);
        var trajCarPlateTask = resetTrajCarPlateTable();
        var trajCarCountTask = resetTrajCarNumCount();
        return CompletableFuture.allOf(trajTableTask, sectionTableTask, eventTableTask, postureTableTask, trajCarPlateTask, trajCarCountTask);
    }

    public CompletableFuture<Void> initTrajTable(String time) {
        return CompletableFuture.runAsync(() -> {
            trajService.dropTable(time);
            trajService.createTable(time);
        }, msgTaskAsyncPool);
    }

    public CompletableFuture<Void> initSectionTable(String time) {
        return CompletableFuture.runAsync(() -> {
            sectionService.dropTable(time);
            sectionService.createTable(time);
        }, msgTaskAsyncPool);
    }

    public CompletableFuture<Void> initEventTable(String time) {
        return CompletableFuture.runAsync(() -> {
            carEventService.dropTable(time);
            carEventService.createTable(time);
        }, msgTaskAsyncPool);
    }

    public CompletableFuture<Void> initPostureTable(String time) {
        return CompletableFuture.runAsync(() -> {
            postureService.dropTable(time);
            postureService.createTable(time);
        }, msgTaskAsyncPool);
    }

    public CompletableFuture<Void> resetTrajCarPlateTable() {
        return CompletableFuture.runAsync(trajCarPlateService::clearTrajCarPlate, msgTaskAsyncPool);
    }

    public CompletableFuture<Void> resetTrajCarNumCount() {
        return CompletableFuture.runAsync(redisTrajFusionService::resetTrajCarNumCount, msgTaskAsyncPool);
    }

}
