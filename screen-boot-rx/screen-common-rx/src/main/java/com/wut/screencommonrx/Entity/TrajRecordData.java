package com.wut.screencommonrx.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrajRecordData {
    private int num;
    private double speed;
    private int type;
}
