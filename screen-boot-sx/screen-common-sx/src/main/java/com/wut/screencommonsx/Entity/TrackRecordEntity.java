package com.wut.screencommonsx.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrackRecordEntity {
    private long trajId;
    private String finalName;
    private String matchName;
}
