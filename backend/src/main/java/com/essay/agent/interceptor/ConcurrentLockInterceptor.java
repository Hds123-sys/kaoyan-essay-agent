package com.essay.agent.interceptor;

import com.essay.agent.common.context.SessionContext;
import com.essay.agent.common.constant.RedisKeyConstants;
import com.essay.agent.common.util.RedisUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class ConcurrentLockInterceptor implements HandlerInterceptor {

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        log.debug("ConcurrentLockInterceptor - TODO: 分布式锁逻辑将在P3实现");
        return true;
    }

}