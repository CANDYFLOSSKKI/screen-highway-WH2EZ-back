package com.wut.screendbmysqlsx.Service.Impl;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wut.screendbmysqlsx.Mapper.LicenseLocMapper;
import com.wut.screendbmysqlsx.Model.LicenseLoc;
import com.wut.screendbmysqlsx.Service.LicenseLocService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LicenseLocServiceImpl extends ServiceImpl<LicenseLocMapper, LicenseLoc> implements LicenseLocService {
    private final LicenseLocMapper licenseLocMapper;

    @Autowired
    public LicenseLocServiceImpl(LicenseLocMapper licenseLocMapper) {
        this.licenseLocMapper = licenseLocMapper;
    }

}
