package com.wut.screencommonsx.Util;

import com.wut.screencommonsx.Entity.CarTypeEntity;
import com.wut.screencommonsx.Response.DefaultDataResp;
import com.wut.screencommonsx.Response.Event.EventStatisticData;
import com.wut.screencommonsx.Response.Posture.PostureFlowTypeData;
import com.wut.screencommonsx.Response.Posture.PostureStatisticData;

import static com.wut.screencommonsx.Static.WebModuleStatic.WEB_RESP_CODE_FAILURE;
import static com.wut.screencommonsx.Static.WebModuleStatic.WEB_RESP_CODE_SUCCESS;

public class ModelTransformUtil {
    public static EventStatisticData getEventStatisticInstance() {
        return new EventStatisticData(0,0,0,0,0,0,0);
    }

    public static PostureStatisticData getPostureStatisticInstance() {
        return new PostureStatisticData(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
    }

    public static DefaultDataResp getDefaultDataInstance(boolean flag, String info, Object data) {
        int code = flag ? WEB_RESP_CODE_SUCCESS : WEB_RESP_CODE_FAILURE;
        return new DefaultDataResp(code, flag, info, data);
    }

    public static PostureFlowTypeData carTypeToPostureFlowTypeData(CarTypeEntity entity) {
        return new PostureFlowTypeData(
                entity.getName(),
                entity.getType(),
                0,
                0,
                0.0,
                0.0
        );
    }

}
