package com.wut.screendbmysqlsx.Service;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wut.screendbmysqlsx.Model.Section;

import java.util.List;

public interface SectionService extends IService<Section> {
    public List<Section> getListByDate(String date);

    public List<Section> getLatestList(String date);

}
