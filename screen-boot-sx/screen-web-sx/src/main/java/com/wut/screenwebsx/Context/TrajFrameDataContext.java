package com.wut.screenwebsx.Context;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wut.screencommonsx.Response.Traj.TrajCarStatisticData;
import com.wut.screencommonsx.Response.Traj.TrajDataResp;
import com.wut.screencommonsx.Response.Traj.TrajInfoData;
import com.wut.screencommonsx.Util.CollectionEmptyUtil;
import com.wut.screencommonsx.Util.DataParamParseUtil;
import com.wut.screencommonsx.Util.DateParamParseUtil;
import com.wut.screencommonsx.Util.MessagePrintUtil;
import com.wut.screendbmysqlsx.Model.Traj;
import com.wut.screendbmysqlsx.Util.DbModelTransformUtil;
import com.wut.screenwebsx.Model.TrajFrameModel;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import static com.wut.screencommonsx.Static.MsgModuleStatic.TOPIC_NAME_TRAJ;
import static com.wut.screencommonsx.Static.WebModuleStatic.*;

@Component
public class TrajFrameDataContext {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final List<TrajInfoData> trajInfoDataListToEZ = new ArrayList<>();
    private final List<TrajInfoData> trajInfoDataListToWH = new ArrayList<>();
    private final AtomicLong recordTimestamp = new AtomicLong(0L);

    @KafkaListener(topics = "traj", groupId = "group-traj")
    public void trajFrameDataListener(List<ConsumerRecord> records, Acknowledgment ack){
        for (ConsumerRecord record : records) {
            try {
                String trajFrameDataStr = record.value().toString();
                TrajFrameModel trajFrameModel = objectMapper.readValue(trajFrameDataStr, TrajFrameModel.class);
                MessagePrintUtil.printListenerReceive(TOPIC_NAME_TRAJ, trajFrameDataStr);
                if (recordTimestamp(trajFrameModel.getTimestamp())) {
                    recordTrajData(trajFrameModel.getTrajList());
                    sendTrajData(trajFrameModel);
                } else {
                    recordTrajData(trajFrameModel.getTrajList());
                }
            } catch (IOException e) {  e.printStackTrace(); }
        }
        ack.acknowledge();
    }

    public void initDataList() {
        trajInfoDataListToEZ.clear();
        trajInfoDataListToWH.clear();
    }

    public boolean recordTimestamp(long newTimestamp) throws IOException{
        long oldTimestamp = recordTimestamp.get();
        long timeInterval = newTimestamp - oldTimestamp;
        if (timeInterval == TRAJ_RECORD_COND) {
            recordTimestamp.compareAndExchange(oldTimestamp, 0L);
            return true;
        }
        // 未找到记录的开始时间/出现超时时,主动记录时间戳(同时清空数据)后返回
        if (oldTimestamp == 0L || timeInterval > TRAJ_RECORD_COND) {
            // 无论客户端有无建立连接,都清除上个刷新时间窗口中保存的轨迹数据
            initDataList();
            recordTimestamp.compareAndExchange(oldTimestamp, newTimestamp - TRAJ_TIME_COND);
        }
        return false;
    }

    public void recordTrajData(List<Traj> trajList) throws IOException{
        if (!CollectionEmptyUtil.forList(trajList)) {
            trajList.stream().forEach(traj -> {
                if (traj.getRoadDirect() == TRAJ_ROAD_DIRECT_TO_EZ) { recordTrajInfo(trajInfoDataListToEZ, traj); }
                if (traj.getRoadDirect() == TRAJ_ROAD_DIRECT_TO_WH) { recordTrajInfo(trajInfoDataListToWH, traj); }
            });
        }
    }

    public void recordTrajInfo(List<TrajInfoData> trajInfoDataList, Traj traj) {
        trajInfoDataList.stream().filter(trajInfoData ->
            Objects.equals(trajInfoData.getTrajId(), traj.getTrajId())
        ).findAny().ifPresentOrElse(
            trajInfoData -> {
                trajInfoData.setDirection(traj.getRoadDirect());
                trajInfoData.setPosition(DataParamParseUtil.getPositionStr(traj.getFrenetX()));
                trajInfoData.setSpeed(traj.getSpeedX() * 3.6);
                trajInfoData.getFrameList().add(DbModelTransformUtil.trajToFrameData(traj));
            },
            () -> {
                TrajInfoData newTrajInfoData = DbModelTransformUtil.trajToInfoData(traj);
                newTrajInfoData.getFrameList().add(DbModelTransformUtil.trajToFrameData(traj));
                trajInfoDataList.add(newTrajInfoData);
            }
        );
    }

    public void sendTrajData(TrajFrameModel trajFrameModel) throws IOException{
        // 当时间戳间隔达到规定的刷新时间时,向客户端推送数据
        WebSocketSession trajSession = WebSocketSessionContext.getSession(FRONT_TRAJ_SESSION_KEY);
        if (trajSession == null) { return; }
        TrajCarStatisticData statisticData = new TrajCarStatisticData(
                trajFrameModel.getCarNumCountToWH(),
                trajFrameModel.getCarNumCountToEZ(),
                trajInfoDataListToWH.size(),
                trajInfoDataListToEZ.size()
        );
        TrajDataResp resp = new TrajDataResp(
                trajFrameModel.getTimestamp(),
                DateParamParseUtil.getDateTimePickerStr(trajFrameModel.getTimestamp()),
                statisticData,
                trajInfoDataListToWH,
                trajInfoDataListToEZ
        );
        trajSession.sendMessage(new TextMessage(objectMapper.writeValueAsString(resp)));
    }

}
