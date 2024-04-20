package com.wut.screencommonrx.Util;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class DataParamParseUtil {
    // 用于筛选集合中指定字段相同的元素,使其成为唯一键
    // 可用于stream().filter()的筛选条件,但无法指定出现相同元素时,具体保留哪个元素的逻辑
    public static <T> Predicate<T> modelDistinctByKey(Function<? super T, ?> keyExtractor) {
        Map<Object,Boolean> map = new ConcurrentHashMap<>();
        return t -> map.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    public static boolean isBetweenRamp(double pos, double x1, double x2) {
        double xMin = Math.min(x1, x2);
        double xMax = Math.max(x1, x2);
        return pos > (xMin - 10) && pos < (xMax + 10);
    }

    public static String getPostureComp(List<Integer> list) {
        return list.stream().map(i -> Integer.toString(i)).collect(
            Collectors.joining("-", "[", "]")
        );
    }

}
