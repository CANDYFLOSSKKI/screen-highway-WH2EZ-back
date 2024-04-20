package com.wut.screenmsgtx.Controller;

import com.wut.screencommontx.Request.DateTimeOrderReq;
import com.wut.screencommontx.Response.DefaultInfoResp;
import com.wut.screencommontx.Util.DataParamParseUtil;
import com.wut.screencommontx.Util.ModelTransformUtil;
import com.wut.screenmsgtx.Context.MsgRedisDataContext;
import com.wut.screenmsgtx.Task.CollectDataTask;
import com.wut.screenmsgtx.Task.TransmitDataTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MsgTaskController {
    private final CollectDataTask collectDataTask;
    private final TransmitDataTask transmitDataTask;
    private final MsgRedisDataContext msgRedisDataContext;

    @Autowired
    public MsgTaskController(CollectDataTask collectDataTask, TransmitDataTask transmitDataTask, MsgRedisDataContext msgRedisDataContext) {
        this.collectDataTask = collectDataTask;
        this.transmitDataTask = transmitDataTask;
        this.msgRedisDataContext = msgRedisDataContext;
    }

    @GetMapping("/connect")
    public DefaultInfoResp getConnectWithDesktop() {
        return ModelTransformUtil.getDefaultInfoON("连接成功");
    }

    @PostMapping("/data/start")
    public DefaultInfoResp msgDataTaskStart(@RequestBody DateTimeOrderReq req) throws Exception{
        if (!DataParamParseUtil.isDateTimeOrderValid(req)) {
            return ModelTransformUtil.getDefaultInfoOFF("启动参数错误");
        }
        msgDataTaskEnd();
        collectDataTask.startTask(req);
        transmitDataTask.startTask();
        return ModelTransformUtil.getDefaultInfoON("启动发送任务");
    }

    @GetMapping("/data/end")
    public DefaultInfoResp msgDataTaskEnd() {
        collectDataTask.endTask();
        transmitDataTask.endTask();
        msgRedisDataContext.resetDataContext();
        return ModelTransformUtil.getDefaultInfoON("终止发送任务");
    }

}
