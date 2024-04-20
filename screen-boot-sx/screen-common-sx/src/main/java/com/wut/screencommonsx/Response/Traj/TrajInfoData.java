package com.wut.screencommonsx.Response.Traj;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrajInfoData {
    @JsonProperty("trajId")
    private long trajId;                        // 轨迹号
    private String license;                     // 车牌号
    private int type;                           // 车型
    private int direction;                      // 道路幅向
    private double speed;                       // 车辆速度(取最新数据)
    private String position;                    // 车辆位置(取最新数据)
    @JsonProperty("frameList")
    private List<TrajFrameData> frameList;      // 车辆轨迹数据列表
}
