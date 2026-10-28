package com.athena.common.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 客户端 IP 解析。
 *
 * <p>只有请求的<b>直接来源</b>位于可信反向代理列表内时，才采信 {@code X-Forwarded-For}；
 * 否则一律使用 {@code request.getRemoteAddr()}。这样可以防止直连客户端伪造审计日志中的 IP。
 *
 * <p>解析 {@code X-Forwarded-For} 时从右向左处理：最右侧是离应用最近的一跳，跳过可信代理后
 * 第一个非可信地址才是真实客户端。即使用 nginx 的 {@code $proxy_add_x_forwarded_for}
 * （会保留客户端自带的 XFF 再追加真实地址），这种方式也不会被伪造值误导。
 */
@Component
public class ClientIpResolver {

    private static final Pattern IPV4_PATTERN = Pattern.compile("^\\d{1,3}(\\.\\d{1,3}){3}$");
    private static final Pattern PORT_PATTERN = Pattern.compile("^\\d{1,5}$");

    private final List<String> trustedProxies;

    public ClientIpResolver(@Value("${athena.security.trusted-proxies:}") String trustedProxies) {
        this.trustedProxies = parseRules(trustedProxies);
    }

    /**
     * 解析请求的真实来源 IP。
     */
    public String resolve(HttpServletRequest request) {
        String remote = normalize(request.getRemoteAddr());
        if (StringUtils.isBlank(remote)) {
            return "";
        }
        // 直接来源不是可信代理：完全忽略转发头，防止伪造
        if (!isTrusted(remote)) {
            return remote;
        }

        String header = request.getHeader("X-Forwarded-For");
        if (StringUtils.isBlank(header)) {
            header = request.getHeader("X-Real-IP");
        }
        if (StringUtils.isBlank(header)) {
            return remote;
        }

        String[] hops = header.split(",");
        // 从右向左：跳过可信代理，取第一个非可信地址
        for (int i = hops.length - 1; i >= 0; i--) {
            String candidate = normalize(hops[i]);
            if (!isValidIp(candidate)) {
                continue;
            }
            if (!isTrusted(candidate)) {
                return candidate;
            }
        }
        // 全部为可信代理或非法值：退回最左侧的合法地址
        for (String hop : hops) {
            String candidate = normalize(hop);
            if (isValidIp(candidate)) {
                return candidate;
            }
        }
        return remote;
    }

    private boolean isTrusted(String ip) {
        for (String rule : trustedProxies) {
            if (matches(ip, rule)) {
                return true;
            }
        }
        return false;
    }

    private static boolean matches(String ip, String rule) {
        if (!rule.contains("/")) {
            return sameIp(ip, rule);
        }
        String[] parts = rule.split("/", 2);
        int bits;
        try {
            bits = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            return false;
        }
        try {
            byte[] addr = InetAddress.getByName(ip).getAddress();
            byte[] network = InetAddress.getByName(parts[0]).getAddress();
            if (addr.length != network.length || bits < 0 || bits > addr.length * 8) {
                return false;
            }
            int fullBytes = bits / 8;
            for (int i = 0; i < fullBytes; i++) {
                if (addr[i] != network[i]) {
                    return false;
                }
            }
            int remainingBits = bits % 8;
            if (remainingBits > 0) {
                int mask = (0xFF << (8 - remainingBits)) & 0xFF;
                if ((addr[fullBytes] & mask) != (network[fullBytes] & mask)) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 按字节比较，避免 "::1" 与 "0:0:0:0:0:0:0:1" 这类等价写法被判为不同。
     */
    private static boolean sameIp(String a, String b) {
        try {
            return Arrays.equals(InetAddress.getByName(a).getAddress(),
                    InetAddress.getByName(b).getAddress());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 规范化：去空白、去端口、去 IPv6 方括号、把 IPv4-mapped IPv6 还原为 IPv4。
     */
    static String normalize(String raw) {
        if (raw == null) {
            return "";
        }
        String value = raw.trim();
        if (value.isEmpty() || "unknown".equalsIgnoreCase(value)) {
            return "";
        }
        if (value.startsWith("[")) {
            int end = value.indexOf(']');
            if (end > 0) {
                value = value.substring(1, end);
            }
        } else {
            int firstColon = value.indexOf(':');
            int lastColon = value.lastIndexOf(':');
            // 只有一个冒号，形如 1.2.3.4:5678
            if (firstColon > 0 && firstColon == lastColon) {
                String host = value.substring(0, firstColon);
                String port = value.substring(firstColon + 1);
                if (IPV4_PATTERN.matcher(host).matches() && PORT_PATTERN.matcher(port).matches()) {
                    value = host;
                }
            }
        }
        if (value.regionMatches(true, 0, "::ffff:", 0, 7)) {
            String mapped = value.substring(7);
            if (IPV4_PATTERN.matcher(mapped).matches()) {
                value = mapped;
            }
        }
        return value;
    }

    static boolean isValidIp(String ip) {
        if (StringUtils.isBlank(ip)) {
            return false;
        }
        if (IPV4_PATTERN.matcher(ip).matches()) {
            for (String octet : ip.split("\\.")) {
                if (Integer.parseInt(octet) > 255) {
                    return false;
                }
            }
            return true;
        }
        // 仅当包含冒号时才按 IPv6 解析，避免触发 DNS 查询
        if (!ip.contains(":")) {
            return false;
        }
        try {
            InetAddress.getByName(ip);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static List<String> parseRules(String raw) {
        List<String> rules = new ArrayList<>();
        if (StringUtils.isBlank(raw)) {
            return rules;
        }
        for (String part : raw.split(",")) {
            String rule = part.trim();
            if (!rule.isEmpty()) {
                rules.add(rule);
            }
        }
        return rules;
    }
}
