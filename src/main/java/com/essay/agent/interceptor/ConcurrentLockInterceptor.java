package com.essay.agent.interceptor;

import com.essay.agent.common.context.SessionContext;
import com.essay.agent.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
public class ConcurrentLockInterceptor implements HandlerInterceptor {

    private final StringRedisTemplate redisTemplate;

    private static final List<String> LOCKED_PATHS = Arrays.asList(
            "/api/essay/correct",
            "/api/essay/re-correct"
    );

    @Autowired
    public ConcurrentLockInterceptor(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();

        if (!shouldLock(path)) {
            return true;
        }

        String sessionId = SessionContext.getSessionId();
        if (sessionId == null) {
            return true;
        }

        String lockKey = "lock:correction:" + sessionId;
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", 5, TimeUnit.MINUTES);

        if (acquired == null || !acquired) {
            log.warn("Correction in progress for sessionId={}", sessionId);
            throw new BusinessException(42902, "批改请求正在处理中，请稍后重试");
        }

        log.debug("Lock acquired for sessionId={}", sessionId);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        String path = request.getRequestURI();
        if (shouldLock(path)) {
            String sessionId = SessionContext.getSessionId();
            if (sessionId != null) {
                String lockKey = "lock:correction:" + sessionId;
                redisTemplate.delete(lockKey);
                log.debug("Lock released for sessionId={}", sessionId);
            }
        }
    }

    private boolean shouldLock(String path) {
        return LOCKED_PATHS.stream().anyMatch(path::startsWith);
    }
}