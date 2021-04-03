package com.huawei.gd.actuator.util;

public class ObjectUtils {
    public static boolean isExist(String clzName) {
        boolean isExist = false;
        try {
            if (Class.forName(clzName) != null) {
                isExist = true;
            }
        } catch (ClassNotFoundException e) {
            // ignore
        }
        return isExist;
    }
}
