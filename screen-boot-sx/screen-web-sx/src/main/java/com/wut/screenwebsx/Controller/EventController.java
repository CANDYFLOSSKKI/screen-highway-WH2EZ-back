package com.wut.screenwebsx.Controller;

import com.wut.screencommonsx.Request.EventTrackReq;
import com.wut.screencommonsx.Response.DefaultDataResp;
import com.wut.screencommonsx.Response.Event.EventDataResp;
import com.wut.screencommonsx.Response.Track.EventTrackDataResp;
import com.wut.screencommonsx.Util.ModelTransformUtil;
import com.wut.screenwebsx.Service.EventWebService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/event")
public class EventController {
    private final EventWebService eventWebService;

    @Autowired
    public EventController(EventWebService eventWebService) {
        this.eventWebService = eventWebService;
    }

    @GetMapping("/data")
    public DefaultDataResp getEventData(@RequestParam("timestamp") String timestamp) {
        EventDataResp data = eventWebService.getEventData(Long.parseLong(timestamp));
        return ModelTransformUtil.getDefaultDataInstance(
                data != null,
                "今日事件统计",
                data
        );
    }

    @PostMapping("/track")
    public DefaultDataResp getEventTrackData(@RequestBody EventTrackReq req) {
        EventTrackDataResp data = eventWebService.getEventTrackData(req);
        return ModelTransformUtil.getDefaultDataInstance(
                data != null,
                "事件信息查询",
                data
        );
    }

}
