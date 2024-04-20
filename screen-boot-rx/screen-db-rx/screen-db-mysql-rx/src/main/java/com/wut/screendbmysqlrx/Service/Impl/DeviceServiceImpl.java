package com.wut.screendbmysqlrx.Service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wut.screendbmysqlrx.Mapper.DeviceMapper;
import com.wut.screendbmysqlrx.Model.Device;
import com.wut.screendbmysqlrx.Service.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DeviceServiceImpl extends ServiceImpl<DeviceMapper, Device> implements DeviceService {
    private final DeviceMapper deviceMapper;

    @Autowired
    public DeviceServiceImpl(DeviceMapper deviceMapper) {
        this.deviceMapper = deviceMapper;
    }

}
