package com.wut.screencommonsx.Response.Event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.wut.screencommonsx.Response.PositionRecordData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventDataResp {
    @JsonProperty("statisticData")
    private EventStatisticData statisticData;               // 事件数量统计
    @JsonProperty("positionRecordList")
    private List<PositionRecordData> positionRecordList;    // 事件路段分布
    @JsonProperty("infoList")
    private List<EventInfoData> infoList;                   // 事件列表
}
