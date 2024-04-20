package com.wut.screencommontx.Util;

import com.wut.screencommontx.Entity.CollectDataParam;
import com.wut.screencommontx.Response.DefaultInfoResp;

public class ModelTransformUtil {
    public static DefaultInfoResp getDefaultInfoON(String info) {
        return new DefaultInfoResp(true, 200, info);
    }

    public static DefaultInfoResp getDefaultInfoOFF(String info) {
        return new DefaultInfoResp(false, 500, info);
    }

    public static CollectDataParam getCollectDataParamOnlyToday(String tableName) {
        return new CollectDataParam(tableName,0,0,0);
    }

    public static CollectDataParam getCollectDataParamNoOffset(String tableName, double timestamp, int limit) {
        return new CollectDataParam(tableName, timestamp, limit, 0);
    }

}
