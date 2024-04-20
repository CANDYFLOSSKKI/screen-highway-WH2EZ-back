package com.wut.screendbtx.Service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wut.screencommontx.Entity.CollectDataParam;
import com.wut.screencommontx.Util.SqlStringConnectUtil;
import com.wut.screendbtx.Context.TableTimeContext;
import com.wut.screendbtx.Mapper.PlateMapper;
import com.wut.screendbtx.Model.Plate;
import com.wut.screendbtx.Service.PlateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.wut.screencommontx.Static.DbModuleStatic.TABLE_SUFFIX_KEY;

@Service
public class PlateServiceImpl extends ServiceImpl<PlateMapper, Plate> implements PlateService {
    private final PlateMapper plateMapper;

    @Autowired
    public PlateServiceImpl(PlateMapper plateMapper) {
        this.plateMapper = plateMapper;
    }

    @Override
    public List<Plate> collectFromToday(CollectDataParam param) {
        TableTimeContext.setTime(TABLE_SUFFIX_KEY, param.getTableName());
        LambdaQueryWrapper<Plate> wrapper = new LambdaQueryWrapper<>();
        wrapper.last(SqlStringConnectUtil.getLastWrapSQL(param.getLimit(),param.getOffset()));
        return plateMapper.selectList(wrapper);
    }

    @Override
    public List<Plate> collectFromTime(CollectDataParam param) {
        TableTimeContext.setTime(TABLE_SUFFIX_KEY, param.getTableName());
        LambdaQueryWrapper<Plate> wrapper = new LambdaQueryWrapper<>();
        wrapper.le(Plate::getGlobalTimestamp,param.getTimestamp());
        wrapper.last(SqlStringConnectUtil.getLastWrapSQL(param.getLimit(),param.getOffset()));
        return plateMapper.selectList(wrapper);
    }

    @Override
    public Plate collectMinTimestamp(CollectDataParam param) {
        TableTimeContext.setTime(TABLE_SUFFIX_KEY, param.getTableName());
        LambdaQueryWrapper<Plate> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Plate::getGlobalTimestamp);
        wrapper.isNotNull(Plate::getGlobalTimestamp);
        wrapper.last(SqlStringConnectUtil.getLimitOneLastWrapSQL());
        return plateMapper.selectOne(wrapper);
    }
}
