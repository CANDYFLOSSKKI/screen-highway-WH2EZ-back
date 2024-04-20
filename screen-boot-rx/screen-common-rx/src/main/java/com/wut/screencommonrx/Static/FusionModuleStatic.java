package com.wut.screencommonrx.Static;

import com.wut.screencommonrx.Entity.EventTypeData;

public class FusionModuleStatic {
    public static final int ASYNC_SERVICE_TIMEOUT = 30000;
    public static final int WAVE_MINIMUM_DATA_SIZE = 2;
    public static final int WAVE_FRENETX_TOLERANCE = 5;
    public static final int WAVE_FRENETY_TOLERANCE = 3;
    public static final int WAVE_SPEED_TOLERANCE = 10;
    public static final double FRENETX_TOLERANCE = 15;
    public static final double FRENETY_TOLERANCE = 3.75;
    public static final int FUSION_TIME_INTER = 200;
    public static final int FUSION_DEL_X_TOLERANCE = 50;
    public static final int FUSION_DEL_Y_TOLERANCE = 8;
    public static final double FRENETX_PRIORITY_RADIO = 0.3;
    public static final double FRENETY_PRIORITY_RADIO = 0.7;
    public static final String DEFAULT_CAR_ID = "0";
    public static final int CAR_TYPE_COUNT = 4;
    public static final int DEFAULT_CAR_TYPE = 1;
    public static final int CAR_CONNECT_TIME_GAP = 20000;
    public static final int CAR_CONNECT_FRENETX_OFFSET = 20;
    public static final int ROAD_DIRECT_TO_EZ = 1;
    public static final int ROAD_DIRECT_TO_WH = 2;
    public static final long TRAJ_DATA_TIME_OFFSET = 600;
    public static final String LASER_MODEL_DEFAULT_TYPE = "1";
    public static final String WAVE_MODEL_DEFAULT_CAR_ID = "0";
    public static final String FIBER_MODEL_DEFAULT_IP = "127.0.0.1";
    public static final String LASER_MODEL_DEFAULT_IP = "127.0.0.1";
    public static final int EVENT_DEFAULT_STATUS = 0;
    public static final int EVENT_DEFAULT_PROCESS = 0;
    public static final long POSTURE_RECORD_TIME_COND = 60000;
    public static final long SECTION_RECORD_TIME_COND = 900000;
    public static final long BATCH_RECORD_TIME_COND = 5000;
    public static final double SECTION_STREAM_SPEED = 110.0;

    public static final EventTypeData EVENT_TYPE_NORMAL = new EventTypeData(0,"",0,50000);
    public static final EventTypeData EVENT_TYPE_PARKING = new EventTypeData(1,"#1",5,5000);
    public static final EventTypeData EVENT_TYPE_AGAINST = new EventTypeData(2,"#2",5,5000);
    public static final EventTypeData EVENT_TYPE_FAST = new EventTypeData(3,"#3",5,10000);
    public static final EventTypeData EVENT_TYPE_SLOW = new EventTypeData(4,"#4",15,10000);
    public static final EventTypeData EVENT_TYPE_OCCUPY = new EventTypeData(5,"#5",25,10000);

}
