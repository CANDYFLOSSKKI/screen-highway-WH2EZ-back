package com.wut.screendbtx.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wut.screencommontx.Entity.CollectDataParam;
import com.wut.screendbtx.Model.Laser;

import java.util.List;

public interface LaserService extends IService<Laser> {
    public List<Laser> collectFromToday(CollectDataParam param);

    public List<Laser> collectFromTime(CollectDataParam param);

    public Laser collectMinTimestamp(CollectDataParam param);

}
