package com.wut.screencommonsx.Util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import static com.wut.screencommonsx.Static.WebModuleStatic.*;

public class DateParamParseUtil {
    public static String getAlignmentNumStr(int num) {
        return num < 10 ? ("0" + num) : Integer.toString(num);
    }

    public static String getDateTimePickerStr(long timestamp) {
        LocalDateTime datetime = new Date(timestamp).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        return datetime.getYear() + DATE_STR_SEPARATOR +
                getAlignmentNumStr(datetime.getMonthValue()) + DATE_STR_SEPARATOR +
                getAlignmentNumStr(datetime.getDayOfMonth()) + DATETIME_STR_SEPARATOR +
                getAlignmentNumStr(datetime.getHour()) + TIME_STR_SEPARATOR +
                getAlignmentNumStr(datetime.getMinute()) + TIME_STR_SEPARATOR +
                getAlignmentNumStr(datetime.getSecond());
    }

    public static String getDateTableStr(long timestamp) {
        LocalDateTime datetime = new Date(timestamp).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        return datetime.getYear() +
                getAlignmentNumStr(datetime.getMonthValue()) +
                getAlignmentNumStr(datetime.getDayOfMonth());
    }

    public static String getTimeDataStr(long timestamp) {
        LocalDateTime datetime = new Date(timestamp).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        return getAlignmentNumStr(datetime.getHour()) + TIME_STR_SEPARATOR +
                getAlignmentNumStr(datetime.getMinute()) + TIME_STR_SEPARATOR +
                getAlignmentNumStr(datetime.getSecond());
    }

    public static String getEventDateTimeDataStr(long timestamp) {
        LocalDateTime datetime = new Date(timestamp).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        return getAlignmentNumStr(datetime.getYear() % 100) + EVENT_DATE_SEPARATOR +
                getAlignmentNumStr(datetime.getMonthValue()) + EVENT_DATE_SEPARATOR +
                getAlignmentNumStr(datetime.getDayOfMonth()) + DATETIME_STR_SEPARATOR + getTimeDataStr(timestamp);
    }

}
