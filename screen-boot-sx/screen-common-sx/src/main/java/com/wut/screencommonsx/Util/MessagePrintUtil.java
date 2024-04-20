package com.wut.screencommonsx.Util;

import java.time.LocalDateTime;

public class MessagePrintUtil {
    public static void printListenerReceive(String key, String data) {
        System.out.println(LocalDateTime.now() + " LISTENER " + key.toUpperCase() + " RECEIVED " + data);
    }

}
