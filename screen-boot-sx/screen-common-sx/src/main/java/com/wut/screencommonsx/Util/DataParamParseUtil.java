package com.wut.screencommonsx.Util;

import java.util.List;
import java.util.regex.Pattern;

import static com.wut.screencommonsx.Static.WebModuleStatic.CAR_TYPE_COUNT;

public class DataParamParseUtil {
    public static String getPositionStr(double frenetx) {
        int pos = Double.valueOf(frenetx).intValue();
        return "K" + (pos / 1000) + (
                frenetx % 1000 == 0 ? "" : ("+" + (pos % 1000))
        );
    }

    public static boolean isBetweenRamp(double pos, double x1, double x2) {
        double xMin = Math.min(x1, x2);
        double xMax = Math.max(x1, x2);
        return pos > (xMin - 10) && pos < (xMax + 10);
    }

    public static int getCarNumCountIndex(int direction, int type) {
        return type - 1 + ((direction - 1) * CAR_TYPE_COUNT);
    }

    public static List<Integer> parsePostureComp(String comp) {
        String readyToSplitStr = comp.substring(1, comp.length() - 1);
        Pattern pattern = Pattern.compile("\\D+");
        return pattern.splitAsStream(readyToSplitStr).mapToInt(Integer::parseInt).boxed().toList();
    }

}
