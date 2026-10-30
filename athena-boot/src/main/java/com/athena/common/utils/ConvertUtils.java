package com.athena.common.utils;

/**
 * @author Mr.sun
 * @date 2022/1/20 10:24
 * @description
 */
public class ConvertUtils {

    public static String getString(Object object) {
        if (object == null || "".equals(object) || "null".equals(object)) {
            return "";
        }
        return (object.toString().trim());
    }

}
