package com.wut.screencommonrx.Util;

import com.wut.screencommonrx.Entity.EventTypeData;
import com.wut.screencommonrx.Model.CarPlateModel;
import com.wut.screencommonrx.Model.MsgSendDataModel.Fiber;
import com.wut.screencommonrx.Model.MsgSendDataModel.Laser;
import com.wut.screencommonrx.Model.MsgSendDataModel.Plate;
import com.wut.screencommonrx.Model.MsgSendDataModel.Wave;
import com.wut.screencommonrx.Model.TrajModel;
import com.wut.screencommonrx.Model.VehicleModel;

import java.util.List;

import static com.wut.screencommonrx.Static.FusionModuleStatic.*;

public class ModelTransformUtil {
    public static TrajModel vehicleToTraj(VehicleModel vehicleModel) {
        TrajModel trajModel = new TrajModel();
        trajModel.setCarType(DEFAULT_CAR_TYPE);
        trajModel.setType(vehicleModel.getType());
        trajModel.setFrenetX(vehicleModel.getFrenetX());
        trajModel.setFrenetY(vehicleModel.getFrenetY());
        trajModel.setSpeedX(vehicleModel.getSpeedX());
        trajModel.setSpeedY(vehicleModel.getSpeedY());
        trajModel.setHeadingAngle(vehicleModel.getHeadingAngle());
        trajModel.setLongitude(vehicleModel.getLongitude());
        trajModel.setLatitude(vehicleModel.getLatitude());
        trajModel.setMercatorX(vehicleModel.getMercatorX());
        trajModel.setMercatorY(vehicleModel.getMercatorY());
        trajModel.setLane(vehicleModel.getLane());
        trajModel.setRawId(vehicleModel.getId());
        trajModel.setTimestamp(vehicleModel.getTimestamp());
        trajModel.setCarId(vehicleModel.getCarId());
        trajModel.setLicenseColor(0);
        trajModel.setAccx(0.0);
        trajModel.setType(vehicleModel.getType());
        trajModel.setRoadDirect(Integer.valueOf(vehicleModel.getRoadDirect()));
        trajModel.setFrenetXPrediction(vehicleModel.getFrenetX() + (vehicleModel.getSpeedX() * FUSION_TIME_INTER / 1000));
        trajModel.setFrenetYPrediction(vehicleModel.getFrenetY());
        return trajModel;
    }

    public static CarPlateModel plateToCarPlate(Plate plate, long timestamp) {
        return new CarPlateModel(
            plate.getIp(),
            plate.getPicId(),
            plate.getGantryId(),
            plate.getCameraNum(),
            plate.getLaneNum(),
            plate.getVehicleId(),
            plate.getPicLicense(),
            plate.getLicenseColor(),
            plate.getVehSpeed().doubleValue(),
            plate.getStart(),
            plate.getEnd(),
            plate.getRoadDirect(),
            plate.getState(),
            plate.getPicTime(),
            plate.getSaveTime(),
            timestamp
        );
    }

    public static VehicleModel fiberToVehicle(Fiber fiber, long timestamp) {
        return new VehicleModel(
            fiber.getCode(),
            FIBER_MODEL_DEFAULT_IP,
            fiber.getId(),
            fiber.getType(),
            fiber.getLength().doubleValue(),
            fiber.getWidth().doubleValue(),
            fiber.getPosX().doubleValue(),
            fiber.getPosY().doubleValue(),
            fiber.getSpeedX(),
            fiber.getSpeedY().doubleValue(),
            fiber.getSpeed(),
            fiber.getAcceleration().doubleValue(),
            fiber.getLongitude(),
            fiber.getLatitude(),
            fiber.getMercatorX(),
            fiber.getMercatorY(),
            fiber.getFrenetX().doubleValue(),
            fiber.getFrenetY(),
            fiber.getHeadingAngle(),
            fiber.getFiberX(),
            fiber.getLane(),
            fiber.getFrenetAngle().doubleValue(),
            fiber.getRoadDirect().toString(),
            fiber.getCarId().toString(),
            timestamp,
            fiber.getSaveTimestamp().longValue()
        );
    }

    public static VehicleModel laserToVehicle(Laser laser, long timestamp) {
        return new VehicleModel(
            laser.getCode(),
            LASER_MODEL_DEFAULT_IP,
            laser.getId(),
            LASER_MODEL_DEFAULT_TYPE,
            laser.getLength().doubleValue(),
            laser.getWidth().doubleValue(),
            laser.getPosX().doubleValue(),
            laser.getPosY().doubleValue(),
            laser.getSpeedX(),
            laser.getSpeedY().doubleValue(),
            laser.getSpeed(),
            laser.getAcceleration().doubleValue(),
            laser.getLongitude(),
            laser.getLatitude(),
            laser.getMercatorX(),
            laser.getMercatorY(),
            laser.getFrenetX(),
            laser.getFrenetY(),
            laser.getHeadingAngle(),
            laser.getFiberX(),
            laser.getLane(),
            laser.getFrenetAngle(),
            laser.getRoadDirect().toString(),
            laser.getCarId().toString(),
            timestamp,
            laser.getSaveTimestamp().longValue()
        );
    }

    public static VehicleModel waveToVehicle(Wave wave, long timestamp, List<Double> frenetPosition) {
        return new VehicleModel(
            wave.getCode(),
            wave.getIp(),
            wave.getId(),
            wave.getType().toString(),
            wave.getLength().doubleValue(),
            wave.getWidth().doubleValue(),
            wave.getPosX().doubleValue(),
            wave.getPosY().doubleValue(),
            wave.getSpeedX(),
            wave.getSpeedY(),
            wave.getSpeed(),
            wave.getAcceleration().doubleValue(),
            wave.getLongitude(),
            wave.getLatitude(),
            wave.getMercatorX(),
            wave.getMercatorY(),
            frenetPosition.get(0),
            frenetPosition.get(1),
            wave.getHeadingAngle(),
            wave.getFiberX(),
            wave.getLane(),
            wave.getFrenetAngle(),
            wave.getRoadDirect().toString(),
                WAVE_MODEL_DEFAULT_CAR_ID,
            timestamp,
            Long.parseLong(wave.getSaveTimestamp())
        );
    }

    public static TrajModel trajModelToFrame(TrajModel trajModel, long timestamp) {
        TrajModel frame = new TrajModel();
        frame.setTimestamp(timestamp);
        frame.setSpeedX(trajModel.getSpeedX());
        frame.setSpeedY(trajModel.getSpeedY());
        double frenetxPre = (trajModel.getSpeedX() * FUSION_TIME_INTER / 1000) + trajModel.getFrenetXPrediction();
        frame.setFrenetXPrediction(frenetxPre);
        frame.setFrenetYPrediction(trajModel.getFrenetYPrediction());
        frame.setFrenetX(trajModel.getFrenetXPrediction());
        frame.setFrenetY(trajModel.getFrenetYPrediction());
        frame.setLongitude(trajModel.getLongitude());
        frame.setLatitude(trajModel.getLatitude());
        frame.setRoadDirect(trajModel.getRoadDirect());
        frame.setTrajId(trajModel.getTrajId());
        frame.setCarId(trajModel.getCarId());
        frame.setLicenseColor(trajModel.getLicenseColor());
        frame.setLane(trajModel.getLane());
        frame.setMercatorX(trajModel.getMercatorX());
        frame.setMercatorY(trajModel.getMercatorY());
        frame.setHeadingAngle(trajModel.getHeadingAngle());
        frame.setRawId(trajModel.getRawId());
        frame.setType(trajModel.getType());
        return frame;
    }

    public static EventTypeData getEventTypeInstance(int value) {
        return switch (value) {
            case 1 -> EVENT_TYPE_PARKING;
            case 2 -> EVENT_TYPE_AGAINST;
            case 3 -> EVENT_TYPE_FAST;
            case 4 -> EVENT_TYPE_SLOW;
            case 5 -> EVENT_TYPE_OCCUPY;
            default -> EVENT_TYPE_NORMAL;
        };
    }

}
