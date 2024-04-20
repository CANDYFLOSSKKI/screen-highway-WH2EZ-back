package com.wut.screendbmysqlsx.Service;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wut.screendbmysqlsx.Model.Traj;

import java.util.List;

public interface TrajService extends IService<Traj> {
    public List<Traj> getListByEventInterval(String date, long timeStart, long timeEnd);

    public List<Traj> getDistinctList(String date);

    public List<Traj> getListByTrajId(String date, long trajId);

}
