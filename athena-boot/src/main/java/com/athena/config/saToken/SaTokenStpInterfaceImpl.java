package com.athena.config.saToken;

import cn.dev33.satoken.stp.StpInterface;
import com.athena.modules.sys.service.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * 权限加载接口实现
 */
@Component
public class SaTokenStpInterfaceImpl implements StpInterface {

    @Autowired
    private SecurityService securityService;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        String username = loginId.toString();
        Set<String> permsSet = securityService.getUserPermissions(username);
        return permsSet.stream().toList();
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        return null;
    }
}
