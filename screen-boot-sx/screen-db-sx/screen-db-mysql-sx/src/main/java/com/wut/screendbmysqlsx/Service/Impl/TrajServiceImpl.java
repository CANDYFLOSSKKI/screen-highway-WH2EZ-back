package com.wut.screendbmysqlsx.Service.Impl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wut.screendbmysqlsx.Context.TableTimeContext;
import com.wut.screendbmysqlsx.Mapper.TrajMapper;
import com.wut.screendbmysqlsx.Model.Traj;
import com.wut.screendbmysqlsx.Service.TrajService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.wut.screencommonsx.Static.DbModuleStatic.TABLE_SUFFIX_KEY;
import static com.wut.screencommonsx.Static.WebModuleStatic.EVENT_TRAJ_TIME_OFFSET;

@Service
public class TrajServiceImpl extends ServiceImpl<TrajMapper, Traj> implements TrajService {
    private final TrajMapper trajMapper;

    @Autowired
    public TrajServiceImpl(TrajMapper trajMapper) {
        this.trajMapper = trajMapper;
    }

    @Override
    public List<Traj> getListByEventInterval(String date, long timeStart, long timeEnd) {
        TableTimeContext.setTime(TABLE_SUFFIX_KEY, date);
        LambdaQueryWrapper<Traj> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(Traj::getTimestamp, timeStart - EVENT_TRAJ_TIME_OFFSET)
                .le(Traj::getTimestamp, timeEnd + EVENT_TRAJ_TIME_OFFSET)
                .orderByAsc(Traj::getTimestamp);
        return trajMapper.selectList(wrapper);
    }

    @Override
    public List<Traj> getDistinctList(String date) {
        TableTimeContext.setTime(TABLE_SUFFIX_KEY, date);
        QueryWrapper<Traj> wrapper = new QueryWrapper<>();
        wrapper.select("DISTINCT CarId, trajId");
        return trajMapper.selectList(wrapper);
    }

    @Override
    public List<Traj> getListByTrajId(String date, long trajId) {
        TableTimeContext.setTime(TABLE_SUFFIX_KEY, date);
        LambdaQueryWrapper<Traj> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Traj::getTrajId, trajId)
                .orderByAsc(Traj::getTimestamp);
        return trajMapper.selectList(wrapper);
    }

}
