package com.wut.screenwebsx.Controller;

import com.wut.screencommonsx.Response.DefaultDataResp;
import com.wut.screencommonsx.Response.Posture.PosturePeriodDataResp;
import com.wut.screencommonsx.Response.Posture.PostureRealTimeDataResp;
import com.wut.screencommonsx.Util.ModelTransformUtil;
import com.wut.screenwebsx.Service.PostureWebService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/posture")
public class PostureController {
    private final PostureWebService postureWebService;

    @Autowired
    public PostureController(PostureWebService postureWebService) {
        this.postureWebService = postureWebService;
    }

    @GetMapping("/data/real")
    public DefaultDataResp getPostureRealTimeData(@RequestParam("timestamp") String timestamp) {
        PostureRealTimeDataResp data = postureWebService.getRealTimePostureData(Long.parseLong(timestamp));
        return ModelTransformUtil.getDefaultDataInstance(
                data != null,
                "实时交通态势",
                data
        );
    }

    @GetMapping("/data/period")
    public DefaultDataResp getPosturePeriodData(@RequestParam("timestamp") String timestamp) {
        PosturePeriodDataResp data = postureWebService.getPeriodPostureData(Long.parseLong(timestamp));
        return ModelTransformUtil.getDefaultDataInstance(
                data != null,
                "今日态势统计",
                data
        );
    }

}
