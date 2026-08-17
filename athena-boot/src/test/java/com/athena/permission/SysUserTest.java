package com.athena.permission;

import cn.dev33.satoken.secure.BCrypt;
import cn.dev33.satoken.secure.SaSecureUtil;
import com.athena.modules.sys.service.SysUserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
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

}
