package com.wut.screendbmysqlrx.Model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("device")
// 雷达设备
public class Device {
    private Integer rid;
    private String name;
    private String ip;
    private Double alpha;
    @TableField("xdistance")
    private Double xDistance;
    @TableField("ydistance")
    private Double yDistance;
    private String remark;
    private String image;
    @TableField("radarAgreement")
    private String radarAgreement;
    private Double longitude;
    private Double latitude;
    @TableField("deflectionAngle")
    private Double deflectionAngle;
    @TableField("roadDirect")
    private Integer roadDirect;
    @TableField("detectStart")
    private Double detectStart;
    @TableField("detectEnd")
    private Double detectEnd;
    private Integer state;
}
