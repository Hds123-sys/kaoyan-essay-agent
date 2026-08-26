package com.essay.agent.interceptor;

import com.essay.agent.common.context.SessionContext;
import com.essay.agent.common.constant.ErrorCodeConstants;
import com.essay.agent.common.constant.RedisKeyConstants;
import com.essay.agent.common.exception.BusinessException;
import com.essay.agent.common.util.RedisUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.regex.Pattern;

@Slf4j
@Component
public class SessionInterceptor implements HandlerInterceptor {

    private static final Pattern UUID_V4_PATTERN = Pattern.compile(
            "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$",
            Pattern.CASE_INSENSITIVE
    );

    private static final String SESSION_ID_HEADER = "X-Session-Id";
    private static final String SESSION_STATUS_HEADER = "X-Session-Status";
    private static final String STATUS_ACTIVE = "active";
    private static final String STATUS_EXPIRED = "expired";

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String sessionId = request.getHeader(SESSION_ID_HEADER);

        if (sessionId == null || sessionId.trim().isEmpty()) {
            throw new BusinessException(ErrorCodeConstants.SESSION_INVALID, "sessionId格式非法");
        }

        if (!UUID_V4_PATTERN.matcher(sessionId).matches()) {
            throw new BusinessException(ErrorCodeConstants.SESSION_INVALID, "sessionId格式非法");
        }

        SessionContext.set(sessionId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        SessionContext.clear();
    }

}