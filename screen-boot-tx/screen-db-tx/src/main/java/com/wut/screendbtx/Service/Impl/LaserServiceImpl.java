package com.wut.screendbtx.Service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wut.screencommontx.Entity.CollectDataParam;
import com.wut.screencommontx.Util.SqlStringConnectUtil;
import com.wut.screendbtx.Context.TableTimeContext;
import com.wut.screendbtx.Mapper.LaserMapper;
import com.wut.screendbtx.Model.Laser;
import com.wut.screendbtx.Service.LaserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.wut.screencommontx.Static.DbModuleStatic.TABLE_SUFFIX_KEY;

@Service
public class LaserServiceImpl extends ServiceImpl<LaserMapper, Laser> implements LaserService {
    private final LaserMapper laserMapper;

    @Autowired
    public LaserServiceImpl(LaserMapper laserMapper) {
        this.laserMapper = laserMapper;
    }

    @Override
    public List<Laser> collectFromToday(CollectDataParam param) {
        TableTimeContext.setTime(TABLE_SUFFIX_KEY, param.getTableName());
        LambdaQueryWrapper<Laser> wrapper = new LambdaQueryWrapper<>();
        wrapper.last(SqlStringConnectUtil.getLastWrapSQL(param.getLimit(),param.getOffset()));
        return laserMapper.selectList(wrapper);
    }

    @Override
    public List<Laser> collectFromTime(CollectDataParam param) {
        TableTimeContext.setTime(TABLE_SUFFIX_KEY, param.getTableName());
        LambdaQueryWrapper<Laser> wrapper = new LambdaQueryWrapper<>();
        wrapper.le(Laser::getTimestamp,param.getTimestamp());
        wrapper.last(SqlStringConnectUtil.getLastWrapSQL(param.getLimit(),param.getOffset()));
        return laserMapper.selectList(wrapper);
    }

    @Override
    public Laser collectMinTimestamp(CollectDataParam param) {
        TableTimeContext.setTime(TABLE_SUFFIX_KEY, param.getTableName());
        LambdaQueryWrapper<Laser> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Laser::getTimestamp);
        wrapper.isNotNull(Laser::getTimestamp);
        wrapper.last(SqlStringConnectUtil.getLimitOneLastWrapSQL());
        return laserMapper.selectOne(wrapper);
    }
}
