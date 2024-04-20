package com.wut.screendbtx.Service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wut.screencommontx.Entity.CollectDataParam;
import com.wut.screencommontx.Util.SqlStringConnectUtil;
import com.wut.screendbtx.Context.TableTimeContext;
import com.wut.screendbtx.Mapper.FiberMapper;
import com.wut.screendbtx.Model.Fiber;
import com.wut.screendbtx.Service.FiberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.wut.screencommontx.Static.DbModuleStatic.TABLE_SUFFIX_KEY;

@Service
public class FiberServiceImpl extends ServiceImpl<FiberMapper, Fiber> implements FiberService {
    private final FiberMapper fiberMapper;

    @Autowired
    public FiberServiceImpl(FiberMapper fiberMapper) {
        this.fiberMapper = fiberMapper;
    }

    @Override
    public List<Fiber> collectFromToday(CollectDataParam param) {
        TableTimeContext.setTime(TABLE_SUFFIX_KEY, param.getTableName());
        LambdaQueryWrapper<Fiber> wrapper = new LambdaQueryWrapper<>();
        wrapper.last(SqlStringConnectUtil.getLastWrapSQL(param.getLimit(),param.getOffset()));
        return fiberMapper.selectList(wrapper);
    }

    @Override
    public List<Fiber> collectFromTime(CollectDataParam param) {
        TableTimeContext.setTime(TABLE_SUFFIX_KEY, param.getTableName());
        LambdaQueryWrapper<Fiber> wrapper = new LambdaQueryWrapper<>();
        wrapper.le(Fiber::getTimestamp,param.getTimestamp());
        wrapper.last(SqlStringConnectUtil.getLastWrapSQL(param.getLimit(),param.getOffset()));
        return fiberMapper.selectList(wrapper);
    }

    @Override
    public Fiber collectMinTimestamp(CollectDataParam param) {
        TableTimeContext.setTime(TABLE_SUFFIX_KEY, param.getTableName());
        LambdaQueryWrapper<Fiber> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Fiber::getTimestamp);
        wrapper.isNotNull(Fiber::getTimestamp);
        wrapper.last(SqlStringConnectUtil.getLimitOneLastWrapSQL());
        return fiberMapper.selectOne(wrapper);
    }
}
