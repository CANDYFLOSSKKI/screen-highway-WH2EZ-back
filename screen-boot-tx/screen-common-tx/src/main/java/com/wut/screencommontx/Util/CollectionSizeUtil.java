package com.wut.screencommontx.Util;

import java.util.List;

public class CollectionSizeUtil {
    public static <T> int forList(List<T> list) {
        if (list == null || list.isEmpty()) { return 0; }
        return list.size();
    }

}
