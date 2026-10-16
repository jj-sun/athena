package com.athena.modules.sys.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.athena.common.utils.Result;
import com.athena.modules.sys.dto.SysRolePermissionDto;
import com.athena.modules.sys.service.SysRolePermissionService;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PostMapping(value = "saveBatch")
    @SaCheckPermission("sys:role:update")
    public Result<Object> saveBatch(@RequestBody SysRolePermissionDto rolePermissionDto) {
        List<String> roleIds = Lists.newArrayList(rolePermissionDto.getRoleId());
        rolePermissionService.deleteBatch(roleIds);
        rolePermissionService.saveOrUpdate(rolePermissionDto.getRoleId(), rolePermissionDto.getPermissionIdList());
        return Result.ok();
    }

    @GetMapping(value = "getPermissionIdList")
    @SaCheckPermission("sys:role:info")
    public Result<Object> getPermissionIdList(@RequestParam(name = "roleId") String roleId) {
        List<String> permissionIds = rolePermissionService.queryPermissionIdList(roleId);
        return Result.ok(permissionIds);
    }

}
