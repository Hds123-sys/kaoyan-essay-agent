package com.essay.agent.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

@Slf4j
@Component
public class SessionInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String sessionId = request.getHeader("X-Session-Id");

        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = UUID.randomUUID().toString();
            log.debug("Generated new sessionId: {}", sessionId);
        }

        com.essay.agent.common.context.SessionContext.set("sessionId", sessionId);
        com.essay.agent.common.context.SessionContext.setSessionId(sessionId);

        response.setHeader("X-Session-Id", sessionId);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        com.essay.agent.common.context.SessionContext.clear();
    }
}