package com.wut.screendbmysqlrx.Service.Impl;

import com.baomidou.mybatisplus.core.batch.MybatisBatch;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wut.screendbmysqlrx.Mapper.TrajCarPlateMapper;
import com.wut.screendbmysqlrx.Model.TrajCarPlate;
import com.wut.screendbmysqlrx.Service.TrajCarPlateService;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

@Service
public class TrajCarPlateServiceImpl extends ServiceImpl<TrajCarPlateMapper, TrajCarPlate> implements TrajCarPlateService {
    private final TrajCarPlateMapper trajCarPlateMapper;
    private final SqlSessionFactory sqlSessionFactory;
    private final TransactionTemplate transactionTemplate;

    @Autowired
    public TrajCarPlateServiceImpl(TrajCarPlateMapper trajCarPlateMapper, SqlSessionFactory sqlSessionFactory, TransactionTemplate transactionTemplate) {
        this.trajCarPlateMapper = trajCarPlateMapper;
        this.sqlSessionFactory = sqlSessionFactory;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public void storeTrajCarPlate(List<TrajCarPlate> trajCarPlateList) {
        transactionTemplate.execute(status -> {
            MybatisBatch<TrajCarPlate> sectionBatchInsert = new MybatisBatch<>(sqlSessionFactory, trajCarPlateList);
            MybatisBatch.Method<TrajCarPlate> method = new MybatisBatch.Method<>(TrajCarPlateMapper.class);
            return sectionBatchInsert.execute(method.insert());
        });
    }

    @Override
    public void clearTrajCarPlate() {
        trajCarPlateMapper.delete(null);
    }

}
