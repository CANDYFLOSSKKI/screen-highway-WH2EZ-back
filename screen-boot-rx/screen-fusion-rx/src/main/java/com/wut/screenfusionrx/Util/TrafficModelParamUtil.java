package com.wut.screenfusionrx.Util;

import com.wut.screencommonrx.Entity.TrajRecordData;
import com.wut.screendbmysqlrx.Model.Traj;

import java.util.Map;

import static com.wut.screencommonrx.Static.FusionModuleStatic.CAR_TYPE_COUNT;

public class TrafficModelParamUtil {
    public static double getTrajRecordSectionArgQ(Map<Long, TrajRecordData> map) {
        return map.size() * 4.0;
    }

    public static double getTrajRecordPostureArgQ(Map<Long, TrajRecordData> map) {
        return map.size() * 60.0;
    }

    public static double getTrajRecordArgV(Map<Long, TrajRecordData> map) {
        return map.values().stream().mapToDouble(record ->
            record.getSpeed() / record.getNum()
        ).sum() * 3.6 / map.size();
    }

    public static void recordTrajToMap(Map<Long, TrajRecordData> map, Traj traj) {
        TrajRecordData trajRecordData = map.get(traj.getTrajId());
        if (trajRecordData == null) {
            map.put(traj.getTrajId(), new TrajRecordData(1, traj.getSpeedX(), traj.getCarType()));
        } else {
            trajRecordData.setNum(trajRecordData.getNum() + 1);
            trajRecordData.setSpeed(trajRecordData.getSpeed() + traj.getSpeedX());
        }
    }

    public static int getCarNumCountIndex(int direction, int type) {
        return type - 1 + ((direction - 1) * CAR_TYPE_COUNT);
    }

}
