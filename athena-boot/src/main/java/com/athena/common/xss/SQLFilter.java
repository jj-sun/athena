package com.athena.common.xss;

import com.athena.common.exception.RRException;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

/**
 * SQL 安全校验
 *
 * @author Mr.sun
 */
public class SQLFilter {

    /**
     * 合法的标识符：字母或下划线开头，后跟字母/数字/下划线，长度不超过 64。
     * 允许一级限定，形如 table.column。
     */
    private static final Pattern IDENTIFIER_PATTERN =
            Pattern.compile("^[A-Za-z_][A-Za-z0-9_]{0,63}(\\.[A-Za-z_][A-Za-z0-9_]{0,63})?$");

    private SQLFilter() {
    }

    /**
     * 校验排序字段，仅接受合法的 SQL 标识符（列名，或 table.column 形式）。
     *
     * <p>原实现是黑名单：先去掉 {@code ' " ; \} 再拦截 9 个关键字。该方式在 ORDER BY 场景下
     * 可以被绕过（{@code sleep( )}、{@code case when ... end}、逗号拼接等都不含被拦截的关键字），
     * 而且它把字段转成了小写，会破坏大小写敏感的数据库上的合法列名。
     * 这里改成白名单：只要出现空白、逗号、括号、引号、分号等任何非标识符字符，直接拒绝。
     *
     * @param str 待校验的排序字段
     * @return 校验通过的字段（原样返回，不做大小写转换）；入参为空时返回 null 表示不排序
     */
    public static String requireOrderField(String str) {
        if (StringUtils.isBlank(str)) {
            return null;
        }
        String field = str.trim();
        if (!IDENTIFIER_PATTERN.matcher(field).matches()) {
            throw new RRException("非法的排序字段");
        }
        return field;
    }

    /**
     * 校验用于 SQL 拼接的表名/列名（如字典翻译中的动态表名、字段名）。
     * 与 {@link #requireOrderField} 共用同一套白名单规则，但不允许为空。
     *
     * @param name 待校验的标识符
     * @param what 出错时的提示对象名，例如“字典表名”
     * @return 校验通过的标识符
     */
    public static String requireIdentifier(String name, String what) {
        if (StringUtils.isBlank(name)) {
            throw new RRException(what + "不能为空");
        }
        String identifier = name.trim();
        if (!IDENTIFIER_PATTERN.matcher(identifier).matches()) {
            throw new RRException("非法的" + what);
        }
        return identifier;
    }
}
