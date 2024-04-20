package com.wut.screendbtx.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wut.screencommontx.Entity.CollectDataParam;
import com.wut.screendbtx.Model.Wave;

import java.util.List;

public interface WaveService extends IService<Wave> {
    public List<Wave> collectFromToday(CollectDataParam param);

    public List<Wave> collectFromTime(CollectDataParam param);

    public Wave collectMinTimestamp(CollectDataParam param);

}
