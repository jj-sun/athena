package com.athena.modules.sys.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.athena.common.exception.RRException;
import com.athena.common.utils.Result;
import com.athena.modules.sys.dto.SysRolePermissionDto;
import com.athena.modules.sys.service.PrivilegeGuardService;
import com.athena.modules.sys.service.SysPermissionService;
import com.athena.modules.sys.service.SysRolePermissionService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Mr.sun
 * @date 2022/1/25 10:52
 * @description
 */
@RestController
@RequestMapping(value = "/sys/rolePermission")
public class SysRolePermissionController {

    @Autowired
    private SysRolePermissionService rolePermissionService;

    @Autowired
    private PrivilegeGuardService privilegeGuard;

    @Autowired
    private SysPermissionService sysPermissionService;

    /**
     * 保存角色的权限集。
     *
     * <p>使用独立权限码 {@code sys:role:assignPermission}：分配权限不等同于"修改角色名称/备注"，
     * 二者必须分开授权。写入前还会校验调用者不得修改自己所属角色、不得授予自己未持有的权限。
     */
    @PostMapping(value = "saveBatch")
    @SaCheckPermission("sys:role:assignPermission")
    public Result<Object> saveBatch(@RequestBody SysRolePermissionDto rolePermissionDto) {
        if (StringUtils.isBlank(rolePermissionDto.getRoleId())) {
            throw new RRException("角色ID不能为空");
        }
        List<String> permissionIdList = rolePermissionDto.getPermissionIdList();
        if (permissionIdList == null) {
            throw new RRException("权限列表不能为空");
        }

        // 仪表盘是所有角色的必选菜单：无论前端是否勾选，保存时都强制补齐
        List<String> finalPermissionIds = new ArrayList<>(permissionIdList);
        finalPermissionIds.addAll(sysPermissionService.getMandatoryMenuIds());
        finalPermissionIds = finalPermissionIds.stream()
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());

        // 授权边界校验（超管不受限）
        privilegeGuard.checkCanAssignPermissions(rolePermissionDto.getRoleId(), finalPermissionIds);

        // saveOrUpdate 内部会先删除该角色原有的权限关联
        rolePermissionService.saveOrUpdate(rolePermissionDto.getRoleId(), finalPermissionIds);
        return Result.ok();
    }

    @GetMapping(value = "getPermissionIdList")
    @SaCheckPermission("sys:role:assignPermission")
    public Result<Object> getPermissionIdList(@RequestParam(name = "roleId") String roleId) {
        // 返回已分配权限 + 强制菜单，角色权限树中仪表盘默认且始终勾选
        List<String> permissionIds = new ArrayList<>(rolePermissionService.queryPermissionIdList(roleId));
        permissionIds.addAll(sysPermissionService.getMandatoryMenuIds());
        return Result.ok(permissionIds.stream().distinct().collect(Collectors.toList()));
    }

}
