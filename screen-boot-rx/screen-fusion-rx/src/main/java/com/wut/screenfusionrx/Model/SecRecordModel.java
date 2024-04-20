package com.wut.screenfusionrx.Model;

import com.wut.screencommonrx.Entity.TrajRecordData;
import com.wut.screendbmysqlrx.Model.Section;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SecRecordModel {
    private Section section;
    private Map<Long, TrajRecordData> trajRecordMapToEZ;
    private Map<Long, TrajRecordData> trajRecordMapToWH;
}
