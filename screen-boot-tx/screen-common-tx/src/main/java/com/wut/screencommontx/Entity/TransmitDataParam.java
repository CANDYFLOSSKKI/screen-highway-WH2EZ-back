package com.wut.screencommontx.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransmitDataParam {
    private long timestamp;
    private Object data;
}
