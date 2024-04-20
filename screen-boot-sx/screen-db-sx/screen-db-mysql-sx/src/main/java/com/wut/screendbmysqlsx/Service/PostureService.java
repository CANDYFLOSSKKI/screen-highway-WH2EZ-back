package com.wut.screendbmysqlsx.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wut.screendbmysqlsx.Model.Posture;

import java.util.List;

public interface PostureService extends IService<Posture> {
    public List<Posture> getListByDate(String date);

    public Posture getLatestOne(String date);

}
