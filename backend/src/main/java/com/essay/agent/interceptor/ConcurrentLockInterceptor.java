package com.essay.agent.interceptor;

import com.essay.agent.common.context.SessionContext;
import com.essay.agent.common.exception.BusinessException;
import com.essay.agent.common.util.RedisLockUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class ConcurrentLockInterceptor implements HandlerInterceptor {

    private static final ThreadLocal<String> LOCK_VALUE = new ThreadLocal<>();

    private static final List<String> LOCK_PATHS = Arrays.asList(
        "/api/correct",
        "/api/generate-topic",
        "/api/generate-reference"
    );

    @Autowired
    private RedisLockUtil redisLockUtil;

    @Value("${agent.lock.timeout-seconds:90}")
    private long timeoutSeconds;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String requestURI = request.getRequestURI();

        if (!needsLock(requestURI)) {
            return true;
        }

        String sessionId = SessionContext.get();
        if (sessionId == null) {
            log.warn("ConcurrentLockInterceptor: sessionId is null");
            return true;
        }

        String lockKey = "lock:session:" + sessionId;
        String lockValue = redisLockUtil.generateLockValue();

        boolean locked = redisLockUtil.tryLock(lockKey, lockValue, timeoutSeconds);
        if (!locked) {
            log.info("ConcurrentLockInterceptor: lock failed for session {}", sessionId);
            throw new BusinessException(42902, "当前有批改任务正在处理，请稍后重试");
        }

        LOCK_VALUE.set(lockValue);
        log.debug("ConcurrentLockInterceptor: acquired lock for session {}", sessionId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        String lockValue = LOCK_VALUE.get();
        if (lockValue != null) {
            String sessionId = SessionContext.get();
            if (sessionId != null) {
                String lockKey = "lock:session:" + sessionId;
                boolean released = redisLockUtil.releaseLock(lockKey, lockValue);
                log.debug("ConcurrentLockInterceptor: released lock for session {}, success={}", sessionId, released);
            }
            LOCK_VALUE.remove();
        }
    }

    private boolean needsLock(String requestURI) {
        return LOCK_PATHS.stream().anyMatch(requestURI::startsWith);
    }
}