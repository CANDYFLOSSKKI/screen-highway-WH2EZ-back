package com.wut.screendbmysqlsx.Model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("licenseloc")
// 牌照设备
public class LicenseLoc {
    private Integer lid;
    @TableField("deviceCode")
    private String deviceCode;
    @TableField("installPlace")
    private String installPlace;
    @TableField("roadDirect")
    private Integer roadDirect;
    private Double start;
    private Double end;
    @TableField("laneNumber")
    private Integer laneNumber;
    private Integer state;
}
