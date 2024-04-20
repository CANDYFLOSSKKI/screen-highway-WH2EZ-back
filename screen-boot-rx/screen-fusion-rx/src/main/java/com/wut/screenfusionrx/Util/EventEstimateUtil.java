package com.wut.screenfusionrx.Util;

import com.wut.screencommonrx.Util.DataParamParseUtil;
import com.wut.screendbmysqlrx.Model.Traj;

import static com.wut.screencommonrx.Static.FusionModuleStatic.ROAD_DIRECT_TO_EZ;
import static com.wut.screencommonrx.Static.FusionModuleStatic.ROAD_DIRECT_TO_WH;

public class EventEstimateUtil {
    public static boolean hasAgainstEvent(Traj traj, double lastFrenetX) {
        if (traj.getRoadDirect() == ROAD_DIRECT_TO_EZ) {
            return traj.getFrenetX() < lastFrenetX;
        } else if (traj.getRoadDirect() == ROAD_DIRECT_TO_WH) {
            return traj.getFrenetX() > lastFrenetX;
        }
        return false;
    }

    public static boolean hasParkingEvent(Traj traj, double firstSpeed, double lastSpeed) {
        return traj.getLane() != 9
                && traj.getSpeedX() * 3.6 < 0.001
                && traj.getFrenetX() > 4300
                && firstSpeed < 10
                && lastSpeed < 5;
    }

    public static boolean hasOccupyEvent(Traj traj) {
        if (traj.getLane() != 9) { return false; }
        double pos = traj.getFrenetX();
        if (traj.getRoadDirect() == 1) {
            if (DataParamParseUtil.isBetweenRamp(pos, 4147.5, 4393.7)
                || DataParamParseUtil.isBetweenRamp(pos, 5342.5, 5266.5)
                || DataParamParseUtil.isBetweenRamp(pos, 12320, 12565)) { return false; }
        } else {
            if (DataParamParseUtil.isBetweenRamp(pos, 12383, 12050)
                || DataParamParseUtil.isBetweenRamp(pos, 5786, 5385)
                || DataParamParseUtil.isBetweenRamp(pos, 4389, 4050)) { return false; }
        }
        return pos > 5400 && pos < 12000;
    }

    public static boolean hasFastEvent(Traj traj, double lastSpeed) {
        return traj.getSpeedX() * 3.6 > 144
                && Math.abs(traj.getSpeedX() * 3.6 - lastSpeed) < 2.5;
    }

    public static boolean hasSlowEvent(Traj traj, double lastSpeed) {
        return traj.getSpeedX() * 3.6 < 60
                && Math.abs(traj.getSpeedX() * 3.6 - lastSpeed) < 2.5
                && traj.getFrenetX() > 5400
                && traj.getFrenetX() < 12000;
    }

}
