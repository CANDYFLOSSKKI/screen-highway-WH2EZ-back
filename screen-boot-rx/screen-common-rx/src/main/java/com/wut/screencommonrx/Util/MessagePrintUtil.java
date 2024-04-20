package com.wut.screencommonrx.Util;

import java.time.LocalDateTime;

public class MessagePrintUtil {
    public static void printListenerReceive(String key, String data) {
        System.out.println(LocalDateTime.now() + " LISTENER " + key.toUpperCase() + " RECEIVED " + data);
    }

    public static void printModelFlush(long timestamp) {
        System.out.println(LocalDateTime.now() + " " + timestamp + " [MODEL DATA FLUSH] PROCESS FINISHED");
    }

    public static void printTrajFlush(long timestamp) {
        System.out.println(LocalDateTime.now() + " " + timestamp + " [TRAJ DATA FUSION] PROCESS FINISHED");
    }

    public static void printSectionData(long timestamp) {
        System.out.println(LocalDateTime.now() + " " + timestamp + " [SECTION DATA COLLECT] PROCESS FINISHED");
    }

    public static void printEventData(long timestamp) {
        System.out.println(LocalDateTime.now() + " " + timestamp + " [EVENT DATA COLLECT] PROCESS FINISHED");
    }

    public static void printDbState(String datetime) {
        System.out.println(LocalDateTime.now() + " [MYSQL DB INITIALIZE] CREATE/RESET TABLE section_" + datetime);
        System.out.println(LocalDateTime.now() + " [MYSQL DB INITIALIZE] CREATE/RESET TABLE carevent_" + datetime);
        System.out.println(LocalDateTime.now() + " [MYSQL DB INITIALIZE] CREATE/RESET TABLE traj_near_real_" + datetime);
        System.out.println(LocalDateTime.now() + " [MYSQL DB INITIALIZE] CREATE/RESET TABLE posture_" + datetime);
    }

    public static void printCarPlateBind(String fore, String after) {
        System.out.println(LocalDateTime.now() + " " + "[TRAJ BIND CARPLATE] SUCCESS ON 【" + fore + " ==> " + after + "】");
    }

    public static void printProducerTransmit(String key, String data) {
        System.out.println(LocalDateTime.now() + " PRODUCER " + key.toUpperCase() + " TRANSMITTED " + data);
    }

    public static void printTrajCacheSave() {
        System.out.println(LocalDateTime.now() + " TRAJ CACHE SAVED TO MYSQL DB");
    }

    public static void printEventCacheSave() {
        System.out.println(LocalDateTime.now() + " EVENT CACHE SAVED TO MYSQL DB");
    }

}
