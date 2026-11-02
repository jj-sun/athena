package com.athena.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 接口文档（springdoc + nextdoc4j）基础配置。
 *
 * <p>springdoc 只消费调用方提供的 {@link OpenAPI}，自己不会创建该 Bean；而 nextdoc4j 的
 * {@code nextdoc4jBasicAuthFilter} 把它声明为必需参数，缺失时应用启动直接失败，因此必须显式提供。
 *
 * <p>同时声明 {@code X-Access-Token} 请求头，让文档页面的调试请求能带上 Sa-Token 令牌。
 *
 * @author Mr.sun
 */
@Configuration(proxyBeanMethods = false)
public class OpenApiConfig {

    /**
     * Sa-Token 的令牌请求头，见 {@code sa-token.token-name}。
     */
    private static final String TOKEN_HEADER = "X-Access-Token";

    @Bean
    public OpenAPI athenaOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Athena 接口文档")
                        .version("3.0.0")
                        .description("Sa-Token 认证：登录取得 token 后填入 Authorize"))
                .components(new Components().addSecuritySchemes(TOKEN_HEADER,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name(TOKEN_HEADER)))
                .addSecurityItem(new SecurityRequirement().addList(TOKEN_HEADER));
    }
}
