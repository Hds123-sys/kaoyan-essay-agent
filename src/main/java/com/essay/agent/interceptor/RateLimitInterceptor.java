package com.essay.agent.interceptor;

import com.essay.agent.common.context.SessionContext;
import com.essay.agent.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final StringRedisTemplate redisTemplate;

    @Value("${agent.rate-limit.per-minute:5}")
    private int rateLimitPerMinute;

    private static final List<String> RATE_LIMITED_PATHS = Arrays.asList(
            "/api/topic/generate",
            "/api/essay/reference",
            "/api/essay/correct",
            "/api/essay/re-correct"
    );

    @Autowired
    public RateLimitInterceptor(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();

        if (!shouldRateLimit(path)) {
            return true;
        }

        String sessionId = SessionContext.getSessionId();
        if (sessionId == null) {
            throw new BusinessException(40301, "会话无效");
        }

        String key = "ratelimit:" + sessionId;
        String count = redisTemplate.opsForValue().get(key);

        if (count == null) {
            redisTemplate.opsForValue().set(key, "1", 60, TimeUnit.SECONDS);
        } else {
            long currentCount = Long.parseLong(count);
            if (currentCount >= rateLimitPerMinute) {
                log.warn("Rate limit exceeded for sessionId={}", sessionId);
                throw new BusinessException(42901, "请求过于频繁，请稍后重试");
            }
            redisTemplate.opsForValue().increment(key);
        }

        log.debug("Rate limit check passed for sessionId={}, path={}, count={}", sessionId, path, count == null ? "1" : String.valueOf(Long.parseLong(count) + 1));

        return true;
    }

    private boolean shouldRateLimit(String path) {
        return RATE_LIMITED_PATHS.stream().anyMatch(path::startsWith);
    }
}