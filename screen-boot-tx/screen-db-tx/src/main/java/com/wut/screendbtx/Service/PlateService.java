package com.wut.screendbtx.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wut.screencommontx.Entity.CollectDataParam;
import com.wut.screendbtx.Model.Plate;

import java.util.List;

public interface PlateService extends IService<Plate> {
    public List<Plate> collectFromToday(CollectDataParam param);

    public List<Plate> collectFromTime(CollectDataParam param);

    public Plate collectMinTimestamp(CollectDataParam param);

}
