package com.athena;

import cn.dev33.satoken.secure.BCrypt;
import cn.dev33.satoken.secure.SaSecureUtil;

/**
 * @author Mr.sun
 * @date 2021/12/14 18:42
 * @description
 */
public class PasswordTest {

    public static void main(String[] args) {
        String md5 = SaSecureUtil.md5("admin:123456");

        //sha256加密
        //String newPassword = BCrypt.hashpw(md5, BCrypt.gensalt());
        //BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
       /* String passworddd = passwordEncoder.encode("admin");
        System.err.println(passworddd);*/
        //boolean res = passwordEncoder.matches("admin", "$2a$10$1TpP8nm7.Jz7RxNl7W99t.JaAtsnX9PfCTPL.TTYaRrDmwV6/LTNW");

        //System.err.println(newPassword);
        System.err.println(BCrypt.checkpw(md5, "$2a$10$/2L6YYEluCtGd3zi63wJsuPBVCBOnT9l..eVrCi10JC4IkabtXRfm"));
    }

}
