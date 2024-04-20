package com.wut.screencommonrx.Util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class DateParamParseUtil {
    public static String getDateTimeStr(long timestamp) {
        LocalDateTime datetime = new Date(timestamp).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        int month = datetime.getMonthValue();
        int day = datetime.getDayOfMonth();
        return datetime.getYear() +
                (month < 10 ? "0" + month : Integer.toString(month)) +
                (day < 10 ? "0" + day : Integer.toString(day));
    }

}
