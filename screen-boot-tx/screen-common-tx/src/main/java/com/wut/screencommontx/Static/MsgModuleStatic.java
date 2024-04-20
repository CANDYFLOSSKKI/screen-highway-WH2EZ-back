package com.wut.screencommontx.Static;

public class MsgModuleStatic {
    public static final String TOPIC_NAME_PLATE = "plate";
    public static final String TOPIC_NAME_FIBER = "fiber";
    public static final String TOPIC_NAME_LASER = "laser";
    public static final String TOPIC_NAME_WAVE = "wave";
    public static final String TOPIC_NAME_TIMESTAMP = "timestamp";
    public static final int TOPIC_PARTITION = 10;
    public static final int TOPIC_REPLICA = 1;
    public static final int TASK_POOL_SIZE = 1;
    public static final int TASK_AWAIT = 60;
    public static final String REDIS_KEY_PLATE_DATA = "plateData";
    public static final String REDIS_KEY_FIBER_DATA = "fiberData";
    public static final String REDIS_KEY_LASER_DATA = "laserData";
    public static final String REDIS_KEY_WAVE_DATA = "waveData";
    public static final String REDIS_KEY_PLATE_OFFSET = "plateOffset";
    public static final String REDIS_KEY_FIBER_OFFSET = "fiberOffset";
    public static final String REDIS_KEY_LASER_OFFSET = "laserOffset";
    public static final String REDIS_KEY_WAVE_OFFSET = "waveOffset";
    public static final String REDIS_KEY_FIND_TODAY = "findToday";
    public static final String REDIS_KEY_COLLECT_TIME = "collectTime";
    public static final String REDIS_KEY_TRANSMIT_TIME = "timestamp";
    public static final String DEFAULT_DATE_PATTERN = "yyyyMMdd";
    public static final String COLLECT_TASK_TIME_CRON = "0/2 * * * * ?";
    public static final int COLLECT_LIMIT = 2000;
    public static final double COLLECT_COND_INTERVAL = 4000;
    public static final int TRANSMIT_TASK_TIME_RATE = 200;
    public static final int TRANSMIT_COND_INTERVAL = 200;
    public static final int ASYNC_SERVICE_TIMEOUT = 10;
    public static final long TRANSMIT_START_TIMEOUT = 5000;
    public static final String CORS_MAPPING = "/**";
    public static final String CORS_HEADERS = "*";
    public static final String CORS_METHODS = "*";
    public static final String CORS_ORIGIN_PATTERNS = "*";
    public static final int CORS_MAX_AGE = 3600;

}
