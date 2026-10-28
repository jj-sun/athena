package com.athena.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 基于 Redis 的固定窗口计数器限流。
 *
 * <p>使用 {@code INCR} + 首次设置过期时间实现：计数超过阈值即拒绝，窗口结束后自动归零。
 */
@Component
public class RateLimitService {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitService.class);

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 尝试获取一次配额。
     *
     * @param key           限流键（应包含业务前缀与来源标识）
     * @param limit         窗口内允许的最大次数
     * @param windowSeconds 窗口长度（秒）
     * @return true 允许；false 超限
     */
    public boolean tryAcquire(String key, int limit, long windowSeconds) {
        if (limit <= 0 || windowSeconds <= 0) {
            return true;
        }
        try {
            Long count = redisTemplate.opsForValue().increment(key);
            if (count == null) {
                return true;
            }
            if (count == 1L) {
                redisTemplate.expire(key, Duration.ofSeconds(windowSeconds));
            }
            return count <= limit;
        } catch (Exception e) {
            // Redis 异常时放行，避免限流组件本身造成登录不可用
            logger.warn("限流计数失败，已放行：key={}, error={}", key, e.getMessage());
            return true;
        }
    }
}
