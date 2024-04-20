package com.wut.screencommontx.Util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import static com.wut.screencommontx.Static.MsgModuleStatic.TRANSMIT_COND_INTERVAL;

public class DateParamParseUtil {
    public static String getDateTimeStr(long timestamp) {
        LocalDateTime datetime = new Date(timestamp).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        String monthStr = datetime.getMonthValue() < 10 ? "0" + datetime.getMonthValue() : Integer.toString(datetime.getMonthValue());
        String dayStr = datetime.getDayOfMonth() < 10 ? "0" + datetime.getDayOfMonth() : Integer.toString(datetime.getDayOfMonth());
        return datetime.getYear() + monthStr + dayStr;
    }

    // 格式化传送的时间戳(查询时间单位200ms),普遍的规则是设为上一轮的时间戳,用于更新偏移量
    // 如果时间戳是200的倍数,发送的数据中最小时间戳就是它本身
    // 如果时间戳不是200的倍数,发送的数据中最小时间戳是它的下一个单位
    public static double getRoundTimestamp(double timestamp) {
        long parseTimestamp = Double.valueOf(timestamp).longValue();
        long last = parseTimestamp % TRANSMIT_COND_INTERVAL;
        return (double)(last == 0 ? parseTimestamp - TRANSMIT_COND_INTERVAL : parseTimestamp - last);
    }

}
