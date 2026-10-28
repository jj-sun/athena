package com.athena.config.saToken;

import java.util.ArrayList;
import java.util.List;

/**
 * 免登录 / 免签名路径的统一出处。
 *
 * <p>{@link SaTokenConfig}（登录拦截器）与 {@link com.athena.config.CorsConfig}（签名过滤器）
 * 都基于这些列表工作。原先两处各写一份，任何一边改动都可能让"免登录"与"免签名"的范围发生偏移，
 * 这里收敛为单一出处。
 */
public final class PublicPaths {

    private PublicPaths() {
    }

    /**
     * 始终免登录的路径。仅限登录/登出/验证码本身。
     */
    private static final List<String> ALWAYS_PUBLIC = List.of(
            "/login",
            "/logout",
            "/captcha"
    );

    /**
     * 静态资源。前端通过 URL 直接引用这些资源时不会携带 token。
     *
     * <p><b>刻意不包含 {@code /**.html}</b>：Knife4j 的 UI 页面（{@code /doc.html}）是随
     * knife4j-openapi3-ui 的 jar 以静态资源形式提供的，即使 {@code knife4j.enable=false}、
     * springdoc 已关闭，该 HTML 外壳仍会被服务出来。之前 {@code /**.html} 在免登录白名单里，
     * 导致生产环境仍能匿名打开文档页面（实测确认）。移除该通配后：dev 环境由 {@link #API_DOCS}
     * 在 {@code expose-api-docs=true} 时单独放行，生产环境则要求登录，文档页面不再公开。
     */
    private static final List<String> STATIC_RESOURCES = List.of(
            "/favicon.ico",
            "/**.js",
            "/**.css",
            "/**.svg",
            "/**.pdf",
            "/**.jpg",
            "/**.png",
            "/**.ico",
            "/**.ttf",
            "/**.woff",
            "/**.woff2"
    );

    /**
     * 接口文档与管理控制台路径。仅在开发环境开放（见 athena.security.expose-api-docs），
     * 生产/测试环境对应端点已被 application-{prod,test}.yml 关闭。
     */
    private static final List<String> API_DOCS = List.of(
            "/doc.html",
            "/swagger-ui.html",
            "/swagger**/**",
            "/webjars/**",
            "/v2/**",
            "/v3/**"
    );

    /**
     * 免登录路径。
     *
     * @param exposeApiDocs 是否放开接口文档路径，由 athena.security.expose-api-docs 控制，仅 dev 为 true
     */
    public static List<String> loginExcludes(boolean exposeApiDocs) {
        List<String> paths = new ArrayList<>(ALWAYS_PUBLIC);
        paths.addAll(STATIC_RESOURCES);
        if (exposeApiDocs) {
            paths.addAll(API_DOCS);
        }
        return List.copyOf(paths);
    }

    /**
     * 免签名校验路径（供 SaServletFilter 使用）。
     *
     * <p>与免登录路径刻意不完全一致：这里额外保留 {@code /druid/**}。Druid 控制台由 starter 注册为
     * 独立 servlet，签名过滤器是 servlet Filter，确实会拦截它；若不放行，浏览器打开控制台时必须
     * 提供无法生成的 sign 参数。测试/生产环境已关闭该 servlet，此条随之失效。
     */
    public static List<String> signExcludes() {
        List<String> paths = new ArrayList<>(ALWAYS_PUBLIC);
        paths.addAll(STATIC_RESOURCES);
        paths.addAll(API_DOCS);
        paths.add("/druid/**");
        return List.copyOf(paths);
    }
}
