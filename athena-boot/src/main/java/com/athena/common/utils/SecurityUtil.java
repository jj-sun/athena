package com.athena.common.utils;

import cn.dev33.satoken.stp.StpUtil;

/**
 * @author Mr.sun
 * @date 2022/2/10 20:11
 * @description
 */
public class SecurityUtil {

    public static String getUserId() {
        try {
            if (!StpUtil.isLogin()) {
                return "";
            }
            Object id = StpUtil.getExtra("id");
            return id == null ? "" : id.toString();
        } catch (Exception e) {
            return "";
        }
    }

    public static String getLoginUsername() {
        try {
            if (!StpUtil.isLogin()) {
                return "";
            }
            return StpUtil.getLoginIdAsString();
        } catch (Exception e) {
            return "";
        }
    }

}
