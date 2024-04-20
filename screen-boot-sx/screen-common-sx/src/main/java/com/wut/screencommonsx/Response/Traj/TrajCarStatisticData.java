package com.wut.screencommonsx.Response.Traj;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrajCarStatisticData {
    @JsonProperty("carNumTotalToWH")
    private int carNumTotalToWH;        // 鄂州至武汉方向累计车辆数
    @JsonProperty("carNumTotalToEZ")
    private int carNumTotalToEZ;        // 武汉至鄂州方向累计车辆数
    @JsonProperty("carNumToWH")
    private int carNumToWH;             // 鄂州至武汉方向在途车辆数
    @JsonProperty("carNumToEZ")
    private int carNumToEZ;             // 武汉至鄂州方向在途车辆数
}
