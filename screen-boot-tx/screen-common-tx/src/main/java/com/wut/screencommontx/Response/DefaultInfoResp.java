package com.wut.screencommontx.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DefaultInfoResp {
    private boolean flag;
    private int code;
    private String info;
}
