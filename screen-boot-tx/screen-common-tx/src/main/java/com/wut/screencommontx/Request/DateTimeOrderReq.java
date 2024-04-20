package com.wut.screencommontx.Request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DateTimeOrderReq {
    private String today;
    private long time;
}
