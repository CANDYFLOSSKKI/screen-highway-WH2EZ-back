package com.wut.screendbmysqlrx.Util;

import com.wut.screencommonrx.Model.CarEventModel;
import com.wut.screencommonrx.Model.TrajModel;
import com.wut.screendbmysqlrx.Model.*;

import java.util.ArrayList;

import static com.wut.screencommonrx.Static.FusionModuleStatic.EVENT_DEFAULT_PROCESS;
import static com.wut.screencommonrx.Static.FusionModuleStatic.EVENT_DEFAULT_STATUS;

public class DbModelTransformUtil {
    public static TrajCarPlate trajModelToCarPlate(TrajModel trajModel) {
        return new TrajCarPlate(
                trajModel.getTrajId(),
                trajModel.getCarId(),
                trajModel.getTimestamp(),
                trajModel.getRoadDirect(),
                0L
        );
    }

    public static Traj trajModelToTraj(TrajModel trajModel) {
        return new Traj(
                trajModel.getTrajId(),
                trajModel.getTimestamp(),
                trajModel.getFrenetX(),
                trajModel.getFrenetY(),
                trajModel.getSpeedX(),
                trajModel.getSpeedY(),
                trajModel.getHeadingAngle(),
                trajModel.getLongitude(),
                trajModel.getLatitude(),
                trajModel.getMercatorX(),
                trajModel.getMercatorY(),
                trajModel.getAccx(),
                trajModel.getRoadDirect(),
                trajModel.getCarId(),
                trajModel.getLicenseColor(),
                trajModel.getLane(),
                trajModel.getType(),
                trajModel.getCarType(),
                trajModel.getRawId()
        );
    }

    public static Section infoToSection(SecInfo secInfo, long timeStart, long timeEnd) {
        return new Section(
                secInfo.getXsecName(),
                secInfo.getXsecValue(),
                timeStart,
                timeEnd,
                0.0,
                0.0,
                0.0,
                0.0,
                0.0,
                0.0
        );
    }

    public static CarEventModel trajToCarEventModel(Traj traj, int type) {
        return new CarEventModel(
                traj.getTrajId(),
                type,
                0,
                traj.getTimestamp(),
                traj.getTimestamp(),
                traj.getCarId(),
                traj.getRoadDirect(),
                traj.getFrenetX(),
                traj.getFrenetX(),
                false,
                false,
                new ArrayList<>()
        );
    }

    public static CarEvent eventModelToEvent(CarEventModel carEventModel) {
        return new CarEvent(
                null,
                carEventModel.getStartTimestamp(),
                carEventModel.getEndTimestamp(),
                carEventModel.getPicLicense(),
                Double.toString(carEventModel.getStartFrenetX()),
                Double.toString(carEventModel.getEndFrenetX()),
                carEventModel.getRoadDirect(),
                carEventModel.getEventType(),
                carEventModel.getTrajId(),
                EVENT_DEFAULT_STATUS,
                EVENT_DEFAULT_PROCESS
        );
    }

    public static Posture getPostureInstance(long timeStart, long timeEnd) {
        return new Posture(
                timeStart,
                timeEnd,
                0.0,
                0.0,
                0.0,
                0.0,
                0.0,
                0.0,
                null
        );
    }

}
