package com.athena.config.saToken;

import cn.dev33.satoken.sign.config.SaSignConfig;
import cn.dev33.satoken.sign.template.SaSignTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 接口签名（防重放）配置 —— 基于 sa-token-sign 插件。
 * <p>
 * 通过独立的 {@link SaSignTemplate} 实例校验请求签名（时间戳窗口 + nonce 去重 + 参数签名），
 * nonce 缓存走 Sa-Token 全局 DAO（本项目为 Redis）。
 * 配置项位于 application.yml 的 {@code sa-token.sign.*}，须与前端 {@code VITE_REPLAY_SECRET} 保持一致。
 *
 * @author Mr.sun
 */
@Configuration
public class SaSignReplayConfig {

    @Value("${sa-token.sign.secret-key:}")
    private String secretKey;

    @Value("${sa-token.sign.digest-algo:sha256}")
    private String digestAlgo;

    @Value("${sa-token.sign.timestamp-disparity:300000}")
    private long timestampDisparity;

    @Bean
    public SaSignTemplate saSignTemplate() {
        SaSignConfig config = new SaSignConfig();
        config.setSecretKey(secretKey);
        config.setDigestAlgo(digestAlgo);
        config.setTimestampDisparity(timestampDisparity);
        return new SaSignTemplate(config);
    }
}
