package com.wut.screendbmysqlrx.Service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wut.screendbmysqlrx.Mapper.SecInfoMapper;
import com.wut.screendbmysqlrx.Model.SecInfo;
import com.wut.screendbmysqlrx.Service.SecInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SecInfoServiceImpl extends ServiceImpl<SecInfoMapper, SecInfo> implements SecInfoService {
    private final SecInfoMapper secInfoMapper;

    @Autowired
    public SecInfoServiceImpl(SecInfoMapper secInfoMapper) {
        this.secInfoMapper = secInfoMapper;
    }

    @Override
    public List<SecInfo> getAllSecInfo() {
        return secInfoMapper.selectList(null);
    }

}
