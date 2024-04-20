package com.wut.screencommonsx.Static;

import java.util.List;

public class DbModuleStatic {
    public static final List<String> DYNAMIC_TABLE_NAMES = List.of(
            "carevent",
            "section",
            "traj_near_real",
            "posture"
    );
    public static final String TABLE_SUFFIX_KEY = "timestamp";
    public static final String TABLE_SUFFIX_SEPARATOR  = "_";
    public static final int TABLE_POSTURE_LIMIT = 60;
    public static final int TABLE_SECTION_LIMIT = 10;
    public static final int TABLE_SECTION_LIST_SIZE = 7;
}
