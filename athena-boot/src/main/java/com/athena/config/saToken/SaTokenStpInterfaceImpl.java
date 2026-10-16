package com.athena.config.saToken;

import cn.dev33.satoken.stp.StpInterface;
import com.athena.common.constant.Constant;
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

    /**
     * 超管通配权限。Sa-Token 校验时用 SaFoxUtil.vagueMatch 逐项匹配，"*" 可匹配任意权限码。
     */
    private static final String ALL_PERMISSION = "*";

    @Autowired
    private SecurityService securityService;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        String username = loginId.toString();
        // 超管直接放行全部权限码。
        // 注意：不能依赖 SecurityServiceImpl.getUserPermissions 的 admin 分支 —— 它返回的是
        // sys_permission 表中【已存在】的 perms 取并集，没有通配语义。若代码中新增的权限码
        // 尚未写入数据库，超管同样会被拒绝，从而锁死整个管理后台。
        if (Constant.SUPER_ADMIN.equals(username)) {
            return List.of(ALL_PERMISSION);
        }
        Set<String> permsSet = securityService.getUserPermissions(username);
        return permsSet.stream().toList();
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        return null;
    }
}
