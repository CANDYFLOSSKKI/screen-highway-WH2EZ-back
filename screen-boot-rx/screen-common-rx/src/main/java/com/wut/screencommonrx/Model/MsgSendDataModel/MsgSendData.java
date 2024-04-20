package com.wut.screencommonrx.Model.MsgSendDataModel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MsgSendData {
    private long timestamp;
    private Object data;
}
