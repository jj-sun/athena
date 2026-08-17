package com.athena.modules.sys.controller;

import com.athena.common.utils.Result;
import com.athena.modules.sys.dto.SysRolePermissionDto;
import com.athena.modules.sys.service.SysRolePermissionService;
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
    public Result<Object> saveBatch(@RequestBody SysRolePermissionDto rolePermissionDto) {
        rolePermissionService.saveOrUpdate(rolePermissionDto.getRoleId(), rolePermissionDto.getPermissionIdList());
        return Result.ok();
    }

    @GetMapping(value = "getPermissionIdList")
    public Result<Object> getPermissionIdList(@RequestParam(name = "roleId") String roleId) {
        List<String> permissionIds = rolePermissionService.queryPermissionIdList(roleId);
        return Result.ok(permissionIds);
    }

}
