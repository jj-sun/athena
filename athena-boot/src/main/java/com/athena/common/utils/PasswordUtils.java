package com.athena.common.utils;

/**
 * 密码工具类
 *
 * @author Mr.sun
 */
public class PasswordUtils {

    /**
     * 密码复杂度规则：大写 + 小写 + 数字 + 特殊字符，且不少于 8 位
     */
    private static final String PASSWORD_PATTERN = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$";

    private PasswordUtils() {
    }

    /**
     * 校验密码是否满足复杂度要求
     *
     * @param password 明文密码
     * @return true 满足复杂度，false 不满足
     */
    public static boolean isComplex(String password) {
        return password != null && password.matches(PASSWORD_PATTERN);
    }
}
