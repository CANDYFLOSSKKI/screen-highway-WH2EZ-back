package com.wut.screenwebsx.Controller;

import com.wut.screencommonsx.Response.DefaultDataResp;
import com.wut.screencommonsx.Response.Section.SecInfoDataResp;
import com.wut.screencommonsx.Response.Section.SectionPeriodDataResp;
import com.wut.screencommonsx.Response.Section.SectionRealTimeDataResp;
import com.wut.screencommonsx.Util.ModelTransformUtil;
import com.wut.screenwebsx.Service.SectionWebService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/section")
public class SectionController {
    private final SectionWebService sectionWebService;

    @Autowired
    public SectionController(SectionWebService sectionWebService) {
        this.sectionWebService = sectionWebService;
    }

    @GetMapping("/data/real")
    public DefaultDataResp getSectionRealTimeData(@RequestParam("timestamp") String timestamp) {
        SectionRealTimeDataResp data = sectionWebService.getSectionRealTimeData(Long.parseLong(timestamp));
        return ModelTransformUtil.getDefaultDataInstance(
                data != null,
                "实时断面数据",
                data
        );
    }

    @GetMapping("/data/period")
    public DefaultDataResp getSectionPeriodData(@RequestParam("timestamp") String timestamp) {
        SectionPeriodDataResp data = sectionWebService.getSectionPeriodData(Long.parseLong(timestamp));
        return ModelTransformUtil.getDefaultDataInstance(
                data != null,
                "今日断面统计",
                data
        );
    }

    @GetMapping("/info")
    public DefaultDataResp getSecInfoData() {
        SecInfoDataResp data = sectionWebService.getSecInfoData();
        return ModelTransformUtil.getDefaultDataInstance(
                data != null,
                "断面选择信息",
                data
        );
    }

}
