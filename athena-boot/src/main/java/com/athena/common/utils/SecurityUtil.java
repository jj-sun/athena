package com.athena.common.utils;

import cn.dev33.satoken.stp.StpUtil;

/**
 * @author Mr.sun
 * @date 2022/2/10 20:11
 * @description
 */
public class SecurityUtil {

    public static String getUserId() {
        //return ((LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser();
        return StpUtil.getExtra("id").toString();
    }

    public static String getLoginUsername() {
        return StpUtil.getLoginIdAsString();
    }

}
