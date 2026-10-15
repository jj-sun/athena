package com.athena.config.saToken;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.jwt.StpLogicJwtForSimple;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handler -> {
                    SaRouter.match("/**")
                            .notMatch("/login")
                            .notMatch("/sys/login")
                            .notMatch("/test/dict/**")
                            .notMatch("/logout")
                            .notMatch("/captcha")
                            .notMatch("/sys/user/passwordChange")
                            .notMatch("/sys/common/static/**")//图片预览 &下载文件不限制token
                            .notMatch("/sys/common/pdf/**")//pdf预览
                            .notMatch("/generic/**")//pdf预览需要文件
                            .notMatch("/doc.html")
                            .notMatch("/**.js")
                            .notMatch("/**.css")
                            .notMatch("/**.html")
                            .notMatch("/**.svg")
                            .notMatch("/**.pdf")
                            .notMatch("/**.jpg")
                            .notMatch("/**.png")
                            .notMatch("/**.ico")
                            .notMatch("/**.ttf")
                            .notMatch("/**.woff")
                            .notMatch("/**.woff2")
                            .notMatch("/druid/**")
                            .notMatch("/swagger-ui.html")
                            .notMatch("/swagger**/**")
                            .notMatch("/webjars/**")
                            .notMatch("/v2/**")
                            .notMatch("/v3/**")
                            .check(r -> StpUtil.checkLogin());
            }))
            .addPathPatterns("/**");
    }


    // Sa-Token 整合 jwt (Simple 简单模式)
    @Bean
    public StpLogic getStpLogicJwt() {
        return new StpLogicJwtForSimple();
    }
}
