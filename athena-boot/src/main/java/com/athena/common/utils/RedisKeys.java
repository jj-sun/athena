package com.athena.common.utils;

/**
 * Redis所有Keys
 *
 * @author Mr.sun
 */
public class RedisKeys {

    public static String getSysConfigKey(String key){
        return "sys:config:" + key;
    }
}
