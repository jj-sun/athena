package com.athena.modules.sys.service;

import java.util.Set;

/**
 * Sa-Token 认证与权限相关接口
 *
 * @author Mr.sun
 */
public interface SecurityService {
    /**
     * 获取用户权限列表
     */
    Set<String> getUserPermissions(String username);

}
