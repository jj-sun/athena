package com.athena.modules.sys.form;

import lombok.Data;

/**
 * 密码表单
 *
 * @author Mr.sun
 */
@Data
public class PasswordForm {

    private String username;

    /**
     * 原密码
     */
    private String password;
    /**
     * 新密码
     */
    private String newPassword;

}
