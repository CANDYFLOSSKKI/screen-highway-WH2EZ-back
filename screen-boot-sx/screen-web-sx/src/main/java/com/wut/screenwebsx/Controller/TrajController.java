package com.wut.screenwebsx.Controller;

import com.wut.screencommonsx.Request.TrajTrackReq;
import com.wut.screencommonsx.Response.DefaultDataResp;
import com.wut.screencommonsx.Response.Track.TrajTrackDataResp;
import com.wut.screencommonsx.Util.ModelTransformUtil;
import com.wut.screenwebsx.Service.TrajWebService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/traj")
public class TrajController {
    private final TrajWebService trajWebService;

    @Autowired
    public TrajController(TrajWebService trajWebService) {
        this.trajWebService = trajWebService;
    }

    @PostMapping("/track")
    public DefaultDataResp getTrajTrackData(@RequestBody TrajTrackReq req) {
        TrajTrackDataResp data = trajWebService.getTrajTrackData(req);
        return ModelTransformUtil.getDefaultDataInstance(
                data != null,
                "历史轨迹查询",
                data
        );
    }

}
