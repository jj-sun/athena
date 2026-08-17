package com.athena.modules.sys.service;

import java.util.List;
import java.util.Set;

/**
 * shiro相关接口
 *
 * @author Mr.sun
 */
public interface SecurityService {
    /**
     * 获取用户权限列表
     */
    Set<String> getUserPermissions(String username);

    List<String> getUserRoles(String username);

}
