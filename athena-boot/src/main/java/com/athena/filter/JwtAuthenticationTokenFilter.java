/*
package com.athena.filter;

import com.athena.common.constant.RedisConstant;
import com.athena.common.constant.SecurityConstant;
import com.athena.common.utils.JwtUtils;
import com.athena.common.utils.RedisUtils;
import com.athena.modules.sys.entity.SysUser;
import com.athena.modules.sys.form.LoginUser;
import com.athena.modules.sys.service.SecurityService;
import com.athena.modules.sys.service.SysUserService;
import com.google.common.collect.Lists;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

*/
/**
 * @author Mr.sun
 * @date 2021/12/14 15:35
 * @description
 *//*

@Component
@Slf4j
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private SecurityService securityService;

    @Resource
    private RedisUtils redisUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String accessToken = request.getHeader(SecurityConstant.X_ACCESS_TOKEN);
        if(StringUtils.isNotBlank(accessToken)) {
            String username = JwtUtils.getUsernameByToken(accessToken);
            if(StringUtils.isBlank(username)) {
                //用户名为空，认证失败
                log.warn("用户名为空，认证失败");
                filterChain.doFilter(request, response);
                return;
            }
            SysUser sysUser = sysUserService.queryByUserName(username);
            if(Objects.isNull(sysUser)) {
                //用户信息为空，认证失败
                log.warn("用户信息为空，认证失败");
                filterChain.doFilter(request, response);
                return;
            }
            if(!jwtTokenRefresh(accessToken, username, sysUser.getPassword())) {
                //token失效，认证失败
                log.warn("token失效，认证失败");
                filterChain.doFilter(request, response);
                return;
            }
            Set<String> perms = securityService.getUserPermissions(sysUser.getUsername());
            LoginUser loginUser = new LoginUser();
            loginUser.setUser(sysUser);
            loginUser.setPermissions(new HashSet<>(perms));
            List<SimpleGrantedAuthority> grantedAuthorities = Lists.newArrayList();
            if(!CollectionUtils.isEmpty(perms)) {
                perms.forEach(perm -> grantedAuthorities.add(new SimpleGrantedAuthority(perm)));
            }
            loginUser.setAuthorityList(grantedAuthorities);
            Authentication authentication = new UsernamePasswordAuthenticationToken(loginUser,accessToken, loginUser.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(request, response);
    }
    */
/**
     * JWTToken刷新生命周期 （实现： 用户在线操作不掉线功能）
     * 1、登录成功后将用户的JWT生成的Token作为k、v存储到cache缓存里面(这时候k、v值一样)，缓存有效期设置为Jwt有效时间的2倍
     * 2、当该用户再次请求时，通过JWTFilter层层校验之后会进入到doGetAuthenticationInfo进行身份验证
     * 3、当该用户这次请求jwt生成的token值已经超时，但该token对应cache中的k还是存在，则表示该用户一直在操作只是JWT的token失效了，程序会给token对应的k映射的v值重新生成JWTToken并覆盖v值，该缓存生命周期重新计算
     * 4、当该用户这次请求jwt在生成的token值已经超时，并在cache中不存在对应的k，则表示该用户账户空闲超时，返回用户信息已失效，请重新登录。
     * 注意： 前端请求Header中设置Authorization保持不变，校验有效性以缓存中的token为准。
     *       用户过期时间 = Jwt有效时间 * 2。
     *
     * @param username 用户名
     * @param password 密码
     * @return true or false
     *//*

    public boolean jwtTokenRefresh(String token, String username, String password) {
        String cacheToken = redisUtils.get(RedisConstant.PREFIX_USER_TOKEN + token);
        if (StringUtils.isNotEmpty(cacheToken)) {
            // 校验token有效性
            if (!JwtUtils.verify(cacheToken, username, password)) {
                String newAuthorization = JwtUtils.generateToken(username, password);
                // 设置超时时间
                redisUtils.set(RedisConstant.PREFIX_USER_TOKEN + token, newAuthorization, JwtUtils.EXPIRE*2 / 1000);
                log.debug("——————————用户在线操作，更新token保证不掉线—————————jwtTokenRefresh——————— "+ token);
            }
            return true;
        }
        return false;
    }
}
*/
