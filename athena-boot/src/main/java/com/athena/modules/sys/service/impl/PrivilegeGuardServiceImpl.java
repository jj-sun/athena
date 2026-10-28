package com.athena.modules.sys.service.impl;

import com.athena.common.constant.Constant;
import com.athena.common.exception.RRException;
import com.athena.common.utils.PermissionCodeUtils;
import com.athena.common.utils.SecurityUtil;
import com.athena.modules.sys.entity.SysPermission;
import com.athena.modules.sys.entity.SysRole;
import com.athena.modules.sys.entity.SysUser;
import com.athena.modules.sys.service.PrivilegeGuardService;
import com.athena.modules.sys.service.SecurityService;
import com.athena.modules.sys.service.SysPermissionService;
import com.athena.modules.sys.service.SysRolePermissionService;
import com.athena.modules.sys.service.SysRoleService;
import com.athena.modules.sys.service.SysUserRoleService;
import com.athena.modules.sys.service.SysUserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * 权限分配边界校验实现。
 *
 * <p>超级管理员（用户名为 {@code admin}）不受限制；其余账号遵循最小权限原则：
 * 只能授予自己已持有的权限码，且不得改动自己所属角色或超级管理员角色。
 */
@Service
public class PrivilegeGuardServiceImpl implements PrivilegeGuardService {

    @Autowired
    private SysRoleService roleService;
    @Autowired
    private SysPermissionService permissionService;
    @Autowired
    private SysUserService userService;
    @Autowired
    private SysUserRoleService userRoleService;
    @Autowired
    private SysRolePermissionService rolePermissionService;
    @Autowired
    private SecurityService securityService;

    private boolean isSuperAdmin() {
        return Constant.SUPER_ADMIN.equals(SecurityUtil.getLoginUsername());
    }

    private SysRole requireRole(String roleId) {
        if (StringUtils.isBlank(roleId)) {
            throw new RRException("角色ID不能为空");
        }
        SysRole role = roleService.getById(roleId);
        if (role == null) {
            throw new RRException("角色不存在");
        }
        return role;
    }

    private boolean isSuperAdminRole(SysRole role) {
        return role != null && Constant.SUPER_ADMIN.equals(role.getRoleCode());
    }

    @Override
    public void checkRoleManageable(String roleId) {
        if (isSuperAdmin()) {
            return;
        }
        if (isSuperAdminRole(requireRole(roleId))) {
            throw new RRException("无权修改超级管理员角色");
        }
    }

    @Override
    public void checkRoleCodeAssignable(String roleCode) {
        if (isSuperAdmin()) {
            return;
        }
        if (Constant.SUPER_ADMIN.equals(roleCode)) {
            throw new RRException("无权使用超级管理员角色编码");
        }
    }

    @Override
    public void checkCanAssignPermissions(String roleId, List<String> permissionIds) {
        if (isSuperAdmin()) {
            return;
        }
        SysRole role = requireRole(roleId);
        if (isSuperAdminRole(role)) {
            throw new RRException("无权修改超级管理员角色的权限");
        }
        // 关键：不允许修改自己所属角色的权限，切断"给自己提权"的直接路径
        if (isCurrentUserInRole(roleId)) {
            throw new RRException("不允许修改自己所属角色的权限");
        }

        Set<String> ownPerms = ownPermissions();
        for (String permissionId : permissionIds == null ? Collections.<String>emptyList() : permissionIds) {
            SysPermission permission = permissionService.getById(permissionId);
            if (permission == null) {
                throw new RRException("权限不存在：" + permissionId);
            }
            for (String code : PermissionCodeUtils.split(permission.getPerms())) {
                // 只允许授予自己已持有的权限码，防止权限放大
                if ("*".equals(code) || !ownPerms.contains(code)) {
                    throw new RRException("无权授予自己不持有的权限：" + code);
                }
            }
        }
    }

    @Override
    public void checkCanAddUsers(String roleId, List<String> userIds) {
        if (isSuperAdmin()) {
            return;
        }
        SysRole role = requireRole(roleId);
        if (isSuperAdminRole(role)) {
            throw new RRException("无权向超级管理员角色添加用户");
        }
        // 不允许给自己分配角色
        String selfId = SecurityUtil.getUserId();
        if (StringUtils.isNotBlank(selfId) && userIds != null && userIds.contains(selfId)) {
            throw new RRException("不允许给自己分配角色");
        }
        // 目标角色的权限必须是调用者权限的子集，否则等于借角色提权
        assertRoleWithinOwnPermissions(roleId);
    }

    @Override
    public void checkCanRemoveUser(String roleId) {
        if (isSuperAdmin()) {
            return;
        }
        if (isSuperAdminRole(requireRole(roleId))) {
            throw new RRException("无权修改超级管理员角色的成员");
        }
    }

    /**
     * 校验目标角色的全部权限码都在调用者自己的权限集内。
     */
    private void assertRoleWithinOwnPermissions(String roleId) {
        Set<String> ownPerms = ownPermissions();
        for (String permissionId : rolePermissionService.queryPermissionIdList(roleId)) {
            SysPermission permission = permissionService.getById(permissionId);
            if (permission == null) {
                continue;
            }
            for (String code : PermissionCodeUtils.split(permission.getPerms())) {
                if ("*".equals(code) || !ownPerms.contains(code)) {
                    throw new RRException("无权把用户加入权限高于自己的角色");
                }
            }
        }
    }

    private Set<String> ownPermissions() {
        return securityService.getUserPermissions(SecurityUtil.getLoginUsername());
    }

    private boolean isCurrentUserInRole(String roleId) {
        SysUser user = userService.queryByUserName(SecurityUtil.getLoginUsername());
        if (user == null) {
            return false;
        }
        List<String> roleIds = userRoleService.queryRoleIdList(user.getId());
        return roleIds != null && roleIds.contains(roleId);
    }
}
