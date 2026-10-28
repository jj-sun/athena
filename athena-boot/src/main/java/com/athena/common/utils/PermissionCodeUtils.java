package com.athena.common.utils;

import com.athena.common.exception.RRException;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 权限标识校验。
 *
 * <p>权限标识最终会参与 Sa-Token 的模糊匹配，其中 {@code *} 是通配符 ——
 * 若允许通过接口写入 {@code perms="*"}，任何能编辑菜单的人都能给自己造出一个"超级权限"。
 * 因此这里只接受形如 {@code sys:user:list} 的普通标识，禁止通配符。
 */
public final class PermissionCodeUtils {

    /** 允许的权限标识字符：字母、数字、下划线、冒号、连字符 */
    private static final Pattern CODE_PATTERN = Pattern.compile("^[A-Za-z0-9_:-]+$");

    private PermissionCodeUtils() {
    }

    /**
     * 校验逗号分隔的权限标识（与 sys_permission.perms 的存储格式一致）。
     * 空值表示该节点没有权限标识（目录/菜单常见），允许。
     *
     * @throws RRException 含通配符或非法字符时抛出
     */
    public static void validatePerms(String perms) {
        for (String code : split(perms)) {
            if (code.contains("*") || !CODE_PATTERN.matcher(code).matches()) {
                throw new RRException("非法的权限标识：" + code);
            }
        }
    }

    /**
     * 拆分逗号分隔的权限标识，并去除空白项。
     */
    public static List<String> split(String perms) {
        List<String> codes = new ArrayList<>();
        if (StringUtils.isBlank(perms)) {
            return codes;
        }
        for (String part : perms.split(",")) {
            String code = part.trim();
            if (!code.isEmpty()) {
                codes.add(code);
            }
        }
        return codes;
    }
}
