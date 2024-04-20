package com.wut.screencommonrx.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventTypeData {
    public int value;
    public String suffix;
    public int time;
    public long timeout;
}
