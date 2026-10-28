package com.athena.common.utils;

import com.athena.common.exception.RRException;
import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 菜单内容字段的安全校验。
 *
 * <p>{@code frameSrc} 最终会进入 {@code <iframe :src>}，{@code icon} 最终会进入前端的
 * {@code <component :is>}。若不校验，拥有菜单编辑权限的人可以写入 {@code javascript:}/
 * {@code data:} 协议或原生标签名，向所有访问该菜单的用户投放任意内容。
 */
public final class WebInputSafetyUtils {

    /** 与库中现有 TDesign 图标名一致的格式：小写字母/数字/连字符 */
    private static final Pattern ICON_PATTERN = Pattern.compile("^[a-z0-9][a-z0-9-]{0,63}$");

    private static final int MAX_FRAME_SRC_LENGTH = 2048;

    private static final Set<String> ALLOWED_SCHEMES = Set.of("http", "https");

    /**
     * 会被 {@code <component :is>} 解析成原生 HTML 标签的名称，必须拒绝。
     */
    private static final Set<String> DANGEROUS_ICON_NAMES = Set.of(
            "script", "iframe", "object", "embed", "img", "image", "svg", "link", "style",
            "base", "meta", "form", "input", "button", "select", "textarea", "video", "audio",
            "source", "track", "template", "slot", "component", "html", "head", "body", "a"
    );

    private WebInputSafetyUtils() {
    }

    /**
     * 校验内嵌地址：只允许 http/https，必须带主机名，且不允许 URL 内嵌凭据。
     *
     * @throws RRException 协议非法、格式错误或超长时抛出
     */
    public static void validateFrameSrc(String frameSrc) {
        if (StringUtils.isBlank(frameSrc)) {
            return;
        }
        String value = frameSrc.trim();
        if (value.length() > MAX_FRAME_SRC_LENGTH) {
            throw new RRException("内嵌地址过长");
        }
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            // 拒绝空白、控制字符：避免 "java\nscript:" 之类的绕过
            if (c <= 0x20 || c == 0x7f) {
                throw new RRException("非法的内嵌地址");
            }
        }

        URI uri;
        try {
            uri = new URI(value);
        } catch (URISyntaxException e) {
            throw new RRException("非法的内嵌地址");
        }

        String scheme = uri.getScheme();
        if (scheme == null || !ALLOWED_SCHEMES.contains(scheme.toLowerCase(Locale.ROOT))) {
            throw new RRException("非法的内嵌地址：仅允许 http/https");
        }
        if (StringUtils.isBlank(uri.getHost())) {
            throw new RRException("非法的内嵌地址：缺少主机名");
        }
        if (uri.getUserInfo() != null) {
            throw new RRException("非法的内嵌地址：不允许包含凭据");
        }
    }

    /**
     * 校验菜单图标名：必须是合法的图标标识，且不能是原生 HTML 标签名。
     *
     * @throws RRException 格式非法时抛出
     */
    public static void validateIcon(String icon) {
        if (StringUtils.isBlank(icon)) {
            return;
        }
        String value = icon.trim();
        if (!ICON_PATTERN.matcher(value).matches() || DANGEROUS_ICON_NAMES.contains(value)) {
            throw new RRException("非法的菜单图标：" + value);
        }
    }
}
