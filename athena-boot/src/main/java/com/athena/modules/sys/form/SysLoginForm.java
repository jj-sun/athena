

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

    //String md5 = SaSecureUtil.md5(user.getUsername() + ":" + user.getPassword());
    private String password;

    //@ApiModelProperty(value = "认证字符串")
    private String auth;
    //@ApiModelProperty(value = "验证码")
    private String captcha;
    //@ApiModelProperty(value = "验证码key")
    private String checkKey;


}
