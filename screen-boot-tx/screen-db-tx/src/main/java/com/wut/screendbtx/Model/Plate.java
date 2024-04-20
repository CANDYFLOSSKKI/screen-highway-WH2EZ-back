package com.wut.screendbtx.Model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
// 相机牌照数据模型
// 对应数据表:carplatev3model_{time}
@TableName("carplatev3model")
public class Plate {
    private Integer id;
    private String ip;
    @TableField("picId")
    @JsonProperty("picId")
    private String picId;
    @TableField("gantryId")
    @JsonProperty("gantryId")
    private String gantryId;        // 门牌序号
    @TableField("cameraNum")
    @JsonProperty("cameraNum")
    private Integer cameraNum;      // 相机编号
    @TableField("laneNum")
    @JsonProperty("laneNum")
    private Integer laneNum;        // 物理车道编码(行驶方向由内向外顺序递增,跨多车道时组合编号)
    @TableField("vehicleId")
    @JsonProperty("vehicleId")
    private Integer vehicleId;      // 车辆编号
    @TableField("picLicense")
    @JsonProperty("picLicense")
    private String picLicense;      // 车牌号码
    @TableField("licenseColor")
    @JsonProperty("licenseColor")
    private Integer licenseColor;   // 车牌颜色
    @TableField("vehSpeed")
    @JsonProperty("vehSpeed")
    private Integer vehSpeed;       // 车辆速度
    private String start;
    private String end;
    @TableField("roadDirect")
    @JsonProperty("roadDirect")
    private String roadDirect;
    private String state;
    @TableField("pictime")
    @JsonProperty("picTime")
    private String picTime;             // 抓拍时间
    @TableField("savetime")
    @JsonProperty("saveTime")
    private String saveTime;
    @TableField("globalTimestamp")
    @JsonProperty("globalTimestamp")
    private Double globalTimestamp;     // 检测时间(unix时间戳,距离1970年时间)
    @TableField("savetimestamp")
    @JsonProperty("saveTimestamp")
    private Double saveTimestamp;
}
