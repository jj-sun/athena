

package com.athena.modules.sys.form;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 登录表单
 *
 * @author Mr.sun
 */
@Data
public class SysLoginForm implements Serializable {

    @Serial
    private static final long serialVersionUID = 5797418362034108255L;

    private String username;

    private String password;

    private String captcha;

    private String checkKey;


}
