package com.wut.screencommontx.Util;

import com.wut.screencommontx.Request.DateTimeOrderReq;

import java.util.Objects;

public class DataParamParseUtil {
    public static boolean isDateTimeOrderValid(DateTimeOrderReq req) {
        return !((req.getToday() == null || Objects.equals(req.getToday(), ""))
                && req.getTime() == 0L);
    }

}
