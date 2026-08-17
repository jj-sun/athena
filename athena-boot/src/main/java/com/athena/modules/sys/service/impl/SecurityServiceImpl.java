package com.athena.modules.sys.service.impl;

import com.athena.common.constant.Constant;
import com.athena.modules.sys.entity.SysPermission;
import com.athena.modules.sys.entity.SysRole;
import com.athena.modules.sys.entity.SysUser;
import com.athena.modules.sys.mapper.SysPermissionMapper;
import com.athena.modules.sys.mapper.SysUserMapper;
import com.athena.modules.sys.service.SecurityService;
import com.athena.modules.sys.service.SysRoleService;
import com.athena.modules.sys.service.SysUserRoleService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author Mr.sun
 */
@Service
public class SecurityServiceImpl implements SecurityService {
    @Autowired
    private SysPermissionMapper sysPermissionMapper;
    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysRoleService roleService;

    @Autowired
    private SysUserRoleService userRoleService;

    @Override
    public Set<String> getUserPermissions(String username) {
        List<String> permsList;

        //系统管理员，拥有最高权限
        if(username.equals(Constant.SUPER_ADMIN)){
            List<SysPermission> menuList = sysPermissionMapper.selectList(null);
            permsList = new ArrayList<>(menuList.size());
            for(SysPermission menu : menuList){
                permsList.add(menu.getPerms());
            }
        }else{
            permsList = sysUserMapper.queryAllPerms(username);
        }
        //用户权限列表
        Set<String> permsSet = new HashSet<>();
        for(String perms : permsList){
            if(StringUtils.isBlank(perms)){
                continue;
            }
            permsSet.addAll(Arrays.asList(perms.trim().split(",")));
        }
        return permsSet;
    }

    @Override
    public List<String> getUserRoles(String username) {
        SysUser sysUser = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
        if(Objects.nonNull(sysUser)) {
            List<String> roleIds = userRoleService.queryRoleIdList(sysUser.getId());
            return roleService.queryRoleCodeById(roleIds);
        }
        return null;
    }
}
