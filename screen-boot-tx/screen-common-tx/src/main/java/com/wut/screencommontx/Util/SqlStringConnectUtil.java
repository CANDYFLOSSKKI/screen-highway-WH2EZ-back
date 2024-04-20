package com.wut.screencommontx.Util;

public class SqlStringConnectUtil {
    public static String getLastWrapSQL(int limit, int offset) {
        return "LIMIT " + limit + " OFFSET " + offset;
    }

    public static String getLimitOneLastWrapSQL() {
        return "LIMIT 1";
    }

}
