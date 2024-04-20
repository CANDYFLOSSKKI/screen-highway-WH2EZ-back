package com.wut.screendbmysqlsx.Util;

import com.wut.screencommonsx.Entity.TrackDistinctEntity;
import com.wut.screencommonsx.Entity.TrackRecordEntity;
import com.wut.screencommonsx.Response.Event.EventInfoData;
import com.wut.screencommonsx.Response.PositionRecordData;
import com.wut.screencommonsx.Response.Section.SecInfoData;
import com.wut.screencommonsx.Response.Section.SectionTimeData;
import com.wut.screencommonsx.Response.Track.EventTrackInfoData;
import com.wut.screencommonsx.Response.Track.TrajTrackInfoData;
import com.wut.screencommonsx.Response.Traj.TrajFrameData;
import com.wut.screencommonsx.Response.Traj.TrajInfoData;
import com.wut.screencommonsx.Util.DataParamParseUtil;
import com.wut.screencommonsx.Util.DateParamParseUtil;
import com.wut.screendbmysqlsx.Model.CarEvent;
import com.wut.screendbmysqlsx.Model.SecInfo;
import com.wut.screendbmysqlsx.Model.Traj;

import java.util.ArrayList;

public class DbModelTransformUtil {
    public static TrajFrameData trajToFrameData(Traj traj) {
        return new TrajFrameData(
                traj.getTimestamp(),
                traj.getLongitude(),
                traj.getLatitude(),
                traj.getHeadingAngle(),
                DataParamParseUtil.getPositionStr(traj.getFrenetX()),
                traj.getSpeedX() * 3.6
        );
    }

    public static TrajInfoData trajToInfoData(Traj traj) {
        return new TrajInfoData(
                traj.getTrajId(),
                traj.getCarId(),
                traj.getCarType(),
                traj.getRoadDirect(),
                traj.getSpeedX() * 3.6,
                DataParamParseUtil.getPositionStr(traj.getFrenetX()),
                new ArrayList<>()
        );
    }

    public static EventInfoData eventToInfoData(CarEvent carEvent) {
        return new EventInfoData(
                carEvent.getUuid(),
                carEvent.getId(),
                carEvent.getTrajId(),
                carEvent.getEventType(),
                DateParamParseUtil.getEventDateTimeDataStr(carEvent.getStartTimestamp()),
                DataParamParseUtil.getPositionStr(Double.parseDouble(carEvent.getStartMileage())),
                carEvent.getStatus(),
                carEvent.getProcess()
        );
    }

    public static PositionRecordData secInfoToPositionRecordData(SecInfo secInfo) {
        return new PositionRecordData(
                secInfo.getXsecName(),
                secInfo.getXsecValue(),
                0.0,
                0.0
        );
    }

    public static SectionTimeData secInfoToSectionTimeData(SecInfo secInfo) {
        return new SectionTimeData(
                secInfo.getXsecName(),
                secInfo.getXsecValue(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>()
        );
    }

    public static EventTrackInfoData trajToTrackInfoData(Traj traj) {
        return new EventTrackInfoData(
                traj.getTrajId(),
                traj.getCarId(),
                traj.getCarType(),
                traj.getRoadDirect(),
                DateParamParseUtil.getTimeDataStr(traj.getTimestamp()),
                traj.getTimestamp(),
                traj.getSpeedX(),
                DataParamParseUtil.getPositionStr(traj.getFrenetX()),
                new ArrayList<>()
        );
    }

    public static TrackDistinctEntity trajToDistinctEntity(Traj traj) {
        return new TrackDistinctEntity(
                traj.getCarId(),
                traj.getTrajId()
        );
    }

    public static TrajTrackInfoData trajAndMatchRecordToTrackInfoData(TrackRecordEntity record, Traj trajStart, Traj trajEnd) {
        return new TrajTrackInfoData(
                record.getTrajId(),
                record.getFinalName(),
                record.getMatchName(),
                trajStart.getCarType(),
                trajStart.getRoadDirect(),
                DateParamParseUtil.getTimeDataStr(trajStart.getTimestamp()),
                trajStart.getTimestamp(),
                DataParamParseUtil.getPositionStr(trajStart.getFrenetX()),
                DateParamParseUtil.getTimeDataStr(trajEnd.getTimestamp()),
                trajEnd.getTimestamp(),
                DataParamParseUtil.getPositionStr(trajEnd.getFrenetX()),
                new ArrayList<>()
        );
    }

    public static SecInfoData secInfoToData(SecInfo secInfo) {
        return new SecInfoData(
                secInfo.getXsecName(),
                secInfo.getXsecValue()
        );
    }

}
