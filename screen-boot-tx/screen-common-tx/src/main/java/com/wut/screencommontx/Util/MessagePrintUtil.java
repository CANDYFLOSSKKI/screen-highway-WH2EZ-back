package com.wut.screencommontx.Util;

import java.time.LocalDateTime;

public class MessagePrintUtil {
    public static void printProducerTransmit(String key, String data) {
        System.out.println(LocalDateTime.now() + " PRODUCER " + key.toUpperCase() + " TRANSMITTED " + data);
    }

}
