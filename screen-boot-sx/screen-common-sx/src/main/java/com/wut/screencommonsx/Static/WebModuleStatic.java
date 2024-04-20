package com.wut.screencommonsx.Static;

import com.wut.screencommonsx.Entity.CarTypeEntity;

public class WebModuleStatic {
    public static final String CORS_MAPPING = "/**";
    public static final String CORS_HEADERS = "*";
    public static final String CORS_METHODS = "*";
    public static final String CORS_ORIGIN_PATTERNS = "*";
    public static final int CORS_MAX_AGE = 3600;
    public static final String FRONT_TRAJ_SESSION_KEY = "front_traj";
    public static final long TRAJ_TIME_COND = 200;
    public static final long TRAJ_RECORD_COND = 5000;
    public static final int TRAJ_ROAD_DIRECT_TO_EZ = 1;
    public static final int TRAJ_ROAD_DIRECT_TO_WH = 2;
    public static final String DATE_STR_SEPARATOR = "-";
    public static final String TIME_STR_SEPARATOR = ":";
    public static final String DATETIME_STR_SEPARATOR = " ";
    public static final String EVENT_DATE_SEPARATOR = "/";
    public static final int EVENT_TYPE_PARKING = 1;
    public static final int EVENT_TYPE_AGAINST = 2;
    public static final int EVENT_TYPE_FAST = 3;
    public static final int EVENT_TYPE_SLOW = 4;
    public static final int EVENT_TYPE_OCCUPY = 5;
    public static final int EVENT_STATUS_PENDING = 0;
    public static final int EVENT_STATUS_FINISHED = 2;
    public static final int ASYNC_SERVICE_TIMEOUT = 30000;
    public static final int WEB_RESP_CODE_SUCCESS = 200;
    public static final int WEB_RESP_CODE_FAILURE = 500;
    public static final int CAR_TYPE_COUNT = 4;
    public static final long EVENT_TRAJ_TIME_OFFSET = 10000;

    public static final CarTypeEntity CAR_TYPE_COMPACT = new CarTypeEntity("小汽车", 1);
    public static final CarTypeEntity CAR_TYPE_TRUCK = new CarTypeEntity("大货车", 2);
    public static final CarTypeEntity CAR_TYPE_BUS = new CarTypeEntity("公共汽车", 3);
    public static final CarTypeEntity CAR_TYPE_SPECIAL = new CarTypeEntity("特殊车辆", 4);
}
