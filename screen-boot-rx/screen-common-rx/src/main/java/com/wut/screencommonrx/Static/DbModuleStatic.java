package com.wut.screencommonrx.Static;

import java.util.List;

public class DbModuleStatic {
    public static final List<String> DYNAMIC_TABLE_NAMES = List.of(
            "carevent",
            "section",
            "traj_near_real",
            "road",
            "posture"
    );
    public static final String TABLE_SUFFIX_SEPARATOR  = "_";
    public static final String TABLE_EVENT_DDL_PREFIX = "carevent" + TABLE_SUFFIX_SEPARATOR;
    public static final String TABLE_SECTION_DDL_PREFIX = "section" + TABLE_SUFFIX_SEPARATOR;
    public static final String TABLE_TRAJ_DDL_PREFIX = "traj_near_real" + TABLE_SUFFIX_SEPARATOR;
    public static final String TABLE_POSTURE_DDL_PREFIX = "posture" + TABLE_SUFFIX_SEPARATOR;
    public static final String TABLE_SUFFIX_KEY = "timestamp";
    public static final String REDIS_KEY_PLATE_MODEL_DATA = "plateModelData";
    public static final String REDIS_KEY_FIBER_MODEL_DATA = "fiberModelData";
    public static final String REDIS_KEY_LASER_MODEL_DATA = "laserModelData";
    public static final String REDIS_KEY_WAVE_MODEL_DATA = "waveModelData";
    public static final String REDIS_KEY_VEHICLE_FLUSH_MODEL = "vehicleFlushModel";
    public static final String REDIS_KEY_TRAJ_MODEL_LINE = "trajModelLine";
    public static final String REDIS_KEY_TRAJ_NEXT_ID = "trajNextId";
    public static final String REDIS_KEY_TRAJ_CAR_NUM_TO_WH = "carNumToWH";
    public static final String REDIS_KEY_TRAJ_CAR_NUM_TO_EZ = "carNumToEZ";
    public static final String REDIS_KEY_TRAJ_DATA = "trajData";
    public static final String REDIS_KEY_EVENT_MODEL_DATA = "eventModelData";
    public static final String REDIS_KEY_DATE_TIME = "dateTime";
    public static final String REDIS_KEY_TRAJ_BATCH_CACHE = "trajBatchCache";
    public static final String REDIS_KEY_EVENT_INSERT_BATCH_CACHE = "eventInsertBatchCache";
    public static final String REDIS_KEY_EVENT_UPDATE_BATCH_CACHE = "eventUpdateBatchCache";

}
