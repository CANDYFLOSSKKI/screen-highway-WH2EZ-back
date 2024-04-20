package com.wut.screendbtx.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wut.screencommontx.Entity.CollectDataParam;
import com.wut.screendbtx.Model.Fiber;

import java.util.List;

public interface FiberService extends IService<Fiber> {
    public List<Fiber> collectFromToday(CollectDataParam param);

    public List<Fiber> collectFromTime(CollectDataParam param);

    public Fiber collectMinTimestamp(CollectDataParam param);

}
