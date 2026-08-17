package com.athena.config;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.filter.SaServletFilter;
import cn.dev33.satoken.router.SaHttpMethod;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
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
    public SaServletFilter saServletFilter() {
        return new SaServletFilter().addInclude("/**")
                .addExclude("/favicon.ico")
                .setAuth(obj -> {
                    SaManager.getLog().debug("----- 请求path={}, 提交token={}", SaHolder.getRequest().getRequestPath(), StpUtil.getTokenValue());
                })
                .setError(e -> {
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