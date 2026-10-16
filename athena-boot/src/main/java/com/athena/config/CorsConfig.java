package com.athena.config;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.filter.SaServletFilter;
import cn.dev33.satoken.router.SaHttpMethod;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.sign.exception.SaSignException;
import cn.dev33.satoken.sign.template.SaSignTemplate;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import com.athena.common.utils.Result;
import com.athena.config.saToken.PublicPaths;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowCredentials(false)
                .allowedMethods("*")
                .maxAge(3600)
                .allowedHeaders("*");

    }

    @Bean
    public SaServletFilter saServletFilter(SaSignTemplate saSignTemplate) {
        // 免签名的路径与登录拦截器共用同一份定义（PublicPaths），避免两处漂移。
        return new SaServletFilter().addInclude("/**")
                .addExclude(PublicPaths.signExcludes().toArray(new String[0]))
                .setAuth(obj -> {
                    SaManager.getLog().debug("----- 请求path={}, 提交token={}", SaHolder.getRequest().getRequestPath(), StpUtil.getTokenValue());
                    // 防重放：时间戳窗口 + nonce 去重 + 参数签名（OPTIONS 预检跳过）
                    if (!SaHolder.getRequest().isMethod(SaHttpMethod.OPTIONS)) {
                        saSignTemplate.checkRequest(SaHolder.getRequest());
                    }
                })
                .setError(e -> {
                    // 签名/重放校验失败，返回统一的 Result 信封（前端读取 message 字段）。
                    // 注意：SaServletFilter 会用 String.valueOf(result) 直接写出响应体，
                    // 因此必须返回 JSON 字符串，而非 Result 对象（否则会写出 Object.toString() 的乱码）。
                    if (e instanceof SaSignException) {
                        return SaManager.getSaJsonTemplate().objectToJson(Result.error(409, e.getMessage()));
                    }
                    return SaResult.error(e.getMessage());
                }).setBeforeAuth(obj -> {
                    SaHolder.getResponse()
                            // ---------- 设置跨域响应头 ----------
                            // 允许指定域访问跨域资源
                            .setHeader("Access-Control-Allow-Origin", "*")
                            // 允许所有请求方式
                            .setHeader("Access-Control-Allow-Methods", "*")
                            // 允许的header参数
                            .setHeader("Access-Control-Allow-Headers", "*")
                            // 有效时间
                            .setHeader("Access-Control-Max-Age", "3600")
                            .setHeader("Access-Control-Allow-Credentials", "false");
                    SaRouter.match(SaHttpMethod.OPTIONS).free(r -> System.out.println("----OPTIONS预检请求，不做处理")).back();
                });
    }

}
