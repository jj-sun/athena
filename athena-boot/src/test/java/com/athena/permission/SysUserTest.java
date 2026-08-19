package com.athena.permission;

import cn.dev33.satoken.secure.BCrypt;
import cn.dev33.satoken.secure.SaSecureUtil;
import com.athena.modules.sys.entity.SysUser;
import com.athena.modules.sys.service.SysUserService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

@ActiveProfiles("dev")
@SpringBootTest
public class SysUserTest {


    @Autowired
    private SysUserService userService;

    @Test
    public void testChangeAdminPassword() {
        String md5 = SaSecureUtil.md5("admin:123456");
        //sha256加密
        String newPassword = BCrypt.hashpw(md5, BCrypt.gensalt());
        userService.updatePassword("admin", newPassword);
    }

    @Test
    public void testGetUserList() {
        List<SysUser> list = userService.list();
        System.err.println(list.toString());
    }

}
