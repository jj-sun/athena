package com.athena.modules.sys.service;

import java.util.List;

/**
 * 权限分配边界校验。
 *
 * <p>角色/权限管理接口不能只校验"是否持有某个权限码"，还必须保证调用者
 * <b>不能把自己或他人提升到高于自己的权限</b>。否则任何持有
 * {@code sys:role:update} 的账号都能通过重写自己角色的权限集或加入 admin 角色，
 * 一步取得管理员等价权限。
 */
public interface PrivilegeGuardService {

    /**
     * 校验当前登录人能否修改/删除目标角色的元数据。
     * 非超级管理员不允许改动超级管理员角色。
     */
    void checkRoleManageable(String roleId);

    /**
     * 校验当前登录人能否把 roleCode 设置为指定值。
     * 非超级管理员不允许使用超级管理员角色编码。
     */
    void checkRoleCodeAssignable(String roleCode);

    /**
     * 校验当前登录人能否重写目标角色的权限集。
     *
     * <ul>
     *   <li>非超级管理员不得修改超级管理员角色；</li>
     *   <li>非超级管理员不得修改自己所属角色的权限；</li>
     *   <li>非超级管理员只能授予自己已持有的权限码。</li>
     * </ul>
     */
    void checkCanAssignPermissions(String roleId, List<String> permissionIds);

    /**
     * 校验当前登录人能否把用户加入目标角色。
     * 非超级管理员不得操作超级管理员角色、不得给自己分配角色，
     * 且目标角色的权限集必须是自己权限的子集。
     */
    void checkCanAddUsers(String roleId, List<String> userIds);

    /**
     * 校验当前登录人能否把用户移出目标角色。
     * 移出只会降低权限，但非超级管理员仍不得改动超级管理员角色。
     */
    void checkCanRemoveUser(String roleId);
}
