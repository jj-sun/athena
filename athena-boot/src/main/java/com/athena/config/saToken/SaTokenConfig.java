package com.athena.config.saToken;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.jwt.StpLogicJwtForSimple;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.router.SaRouterStaff;
import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    /**
     * 是否放开接口文档路径（仅开发环境应为 true）。
     */
    @Value("${athena.security.expose-api-docs:false}")
    private boolean exposeApiDocs;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handler -> {
                    SaRouterStaff staff = SaRouter.match("/**");
                    for (String path : PublicPaths.loginExcludes(exposeApiDocs)) {
                        staff.notMatch(path);
                    }
                    staff.check(r -> StpUtil.checkLogin());
            }))
            .addPathPatterns("/**");
    }


    // Sa-Token 整合 jwt (Simple 简单模式)
    @Bean
    public StpLogic getStpLogicJwt() {
        return new StpLogicJwtForSimple();
    }
}
