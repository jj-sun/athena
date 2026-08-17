package com.athena.modules.sys.form;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Mr.sun
 * @date 2021/12/14 11:31
 * @description
 */
@Data
public class LoginUser implements Serializable {

    @Serial
    private static final long serialVersionUID = -3519083953528868241L;

    private String id;
    private String username;

    private String realname;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String mobile;

    /**
     * 性别
     */
    private Integer sex;

    /**
     * 部门code
     */
    private String deptCode;

    private Integer userType;

}
