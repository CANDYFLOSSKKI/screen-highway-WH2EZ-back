package com.wut.screendbtx.Service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wut.screencommontx.Entity.CollectDataParam;
import com.wut.screencommontx.Util.SqlStringConnectUtil;
import com.wut.screendbtx.Context.TableTimeContext;
import com.wut.screendbtx.Mapper.WaveMapper;
import com.wut.screendbtx.Model.Wave;
import com.wut.screendbtx.Service.WaveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.wut.screencommontx.Static.DbModuleStatic.TABLE_SUFFIX_KEY;

@Service
public class WaveServiceImpl extends ServiceImpl<WaveMapper, Wave> implements WaveService {
    private final WaveMapper waveMapper;

    @Autowired
    public WaveServiceImpl(WaveMapper waveMapper) {
        this.waveMapper = waveMapper;
    }

    @Override
    public List<Wave> collectFromToday(CollectDataParam param) {
        TableTimeContext.setTime(TABLE_SUFFIX_KEY, param.getTableName());
        LambdaQueryWrapper<Wave> wrapper = new LambdaQueryWrapper<>();
        wrapper.last(SqlStringConnectUtil.getLastWrapSQL(param.getLimit(),param.getOffset()));
        return waveMapper.selectList(wrapper);
    }

    @Override
    public List<Wave> collectFromTime(CollectDataParam param) {
        TableTimeContext.setTime(TABLE_SUFFIX_KEY, param.getTableName());
        LambdaQueryWrapper<Wave> wrapper = new LambdaQueryWrapper<>();
        wrapper.le(Wave::getTimestamp,param.getTimestamp());
        wrapper.last(SqlStringConnectUtil.getLastWrapSQL(param.getLimit(),param.getOffset()));
        return waveMapper.selectList(wrapper);
    }

    @Override
    public Wave collectMinTimestamp(CollectDataParam param) {
        TableTimeContext.setTime(TABLE_SUFFIX_KEY, param.getTableName());
        LambdaQueryWrapper<Wave> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Wave::getTimestamp);
        wrapper.isNotNull(Wave::getTimestamp);
        wrapper.last(SqlStringConnectUtil.getLimitOneLastWrapSQL());
        return waveMapper.selectOne(wrapper);
    }
}
