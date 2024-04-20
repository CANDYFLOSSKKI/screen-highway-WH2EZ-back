package com.wut.screencommontx.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CollectDataParam {
    private String tableName;
    private double timestamp;
    private int limit;
    private int offset;
}
