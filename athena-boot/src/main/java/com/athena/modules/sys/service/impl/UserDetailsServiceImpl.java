/*
package com.athena.modules.sys.service.impl;

import com.athena.modules.sys.entity.SysUser;
import com.athena.modules.sys.form.LoginUser;
import com.athena.modules.sys.service.SecurityService;
import com.athena.modules.sys.service.SysUserService;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.Set;

*/
/**
 * @author Mr.sun
 * @date 2021/12/14 11:32
 * @description
 *//*

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private SecurityService securityService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser sysUser = sysUserService.queryByUserName(username);
        if(Objects.isNull(sysUser)) {
            throw new UsernameNotFoundException("找不到用户信息！");
        }
        LoginUser userDetail = new LoginUser();
        userDetail.setUser(sysUser);
        Set<String> permsSet = securityService.getUserPermissions(username);
        List<GrantedAuthority> grantedAuthorities = Lists.newArrayList();
        if(!CollectionUtils.isEmpty(permsSet)) {
            userDetail.setPermissions(permsSet);
            permsSet.forEach(perm -> grantedAuthorities.add(new SimpleGrantedAuthority(perm)));
            userDetail.setAuthorityList(grantedAuthorities);
        }
        return userDetail;
    }

}
*/
