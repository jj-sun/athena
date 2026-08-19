package com.athena.permission;

import com.athena.modules.sys.entity.SysPermission;
import com.athena.modules.sys.service.SysPermissionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mr.sun
 * @date 2021/12/28 11:49
 * @description
 */
@ActiveProfiles("dev")
@SpringBootTest
public class PermissionTest {

    @Autowired
    private SysPermissionService permissionService;

    @Test
    public void inertRoot() {
        SysPermission sysPermission = new SysPermission();
        sysPermission.setParentId("0");
        sysPermission.setName("首页");
        sysPermission.setUrl("dashboard/analysis");
        sysPermission.setComponent("/dashboard/index");
        sysPermission.setType(0);

        permissionService.save(sysPermission);
    }
    @Test
    public void addRootChildren() {
        SysPermission sysPermission = new SysPermission();
        sysPermission.setParentId("1475678576111116290");
        sysPermission.setName("统计报表");
        sysPermission.setUrl("dashboard/report");
        sysPermission.setComponent("/dashboard/report");
        sysPermission.setType(1);

        permissionService.save(sysPermission);
    }

    @Test
    public void insertSystem(){
        SysPermission sysPermission = new SysPermission();
        sysPermission.setParentId("0");
        sysPermission.setName("系统管理");
        sysPermission.setUrl("/system");
        sysPermission.setComponent("layout/index");
        sysPermission.setType(1);
        permissionService.save(sysPermission);
    }

    public List<SysPermission> buildSystemMenu() {
        List<SysPermission> list = new ArrayList<>();
        SysPermission user = new SysPermission();
        user.setParentId("1475702165283561473");
        user.setName("用户管理");
        user.setUrl("/system/user");
        user.setComponent("system/userList");
        user.setType(0);

        SysPermission role = new SysPermission();
        role.setParentId("1475702165283561473");
        role.setName("角色管理");
        role.setUrl("/system/role");
        role.setComponent("system/roleList");
        role.setType(1);

        SysPermission menu = new SysPermission();
        menu.setParentId("1475702165283561473");
        menu.setName("菜单管理");
        menu.setUrl("/system/permission");
        menu.setComponent("system/permissionList");
        menu.setType(2);

        list.add(user);
        list.add(role);
        list.add(menu);
        return list;
    }

    @Test
    public void insertSystemChildren() {
        List<SysPermission> list = buildSystemMenu();
        permissionService.saveBatch(list);
    }

}
