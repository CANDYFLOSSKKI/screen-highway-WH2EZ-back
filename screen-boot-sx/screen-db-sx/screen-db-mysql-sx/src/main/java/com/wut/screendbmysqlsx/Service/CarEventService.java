package com.wut.screendbmysqlsx.Service;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wut.screendbmysqlsx.Model.CarEvent;

import java.util.List;

public interface CarEventService extends IService<CarEvent> {
    public List<CarEvent> getListByDate(String date);

    public CarEvent getOneByUuid(String date, long uuid);

}
