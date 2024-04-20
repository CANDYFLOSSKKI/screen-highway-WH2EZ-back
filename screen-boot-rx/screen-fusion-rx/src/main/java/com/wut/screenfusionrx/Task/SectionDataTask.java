package com.wut.screenfusionrx.Task;

import com.wut.screendbmysqlrx.Model.Section;
import com.wut.screenfusionrx.Context.SectionDataContext;
import com.wut.screenfusionrx.Service.SectionDataService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class SectionDataTask {
    private final SectionDataService sectionDataService;
    private final SectionDataContext sectionDataContext;
    @Qualifier("fusionTaskAsyncPool")
    private final Executor fusionTaskAsyncPool;
    private static final ReentrantLock SECTION_DATA_LOCK = new ReentrantLock();

    @Autowired
    public SectionDataTask(SectionDataService sectionDataService, SectionDataContext sectionDataContext, Executor fusionTaskAsyncPool) {
        this.sectionDataService = sectionDataService;
        this.sectionDataContext = sectionDataContext;
        this.fusionTaskAsyncPool = fusionTaskAsyncPool;
    }

    @RabbitListener(queues = "section")
    public void sectionDataListener(String timestampStr) {
        startParseSectionData(Long.parseLong(timestampStr)).thenRunAsync(() -> {});
    }

    public CompletableFuture<Void> startParseSectionData(long timestamp) {
        return CompletableFuture.runAsync(() -> {
            try {
                SECTION_DATA_LOCK.lock();
                if (!sectionDataContext.recordTimestamp(timestamp)) { return; }
                List<Section> sectionList = sectionDataService.collectSectionData(timestamp);
                sectionDataService.storeSectionData(sectionList, timestamp).thenRunAsync(() -> {});
            } catch (Exception e) { e.printStackTrace(); }
            finally { SECTION_DATA_LOCK.unlock(); }
        }, fusionTaskAsyncPool);
    }

}
