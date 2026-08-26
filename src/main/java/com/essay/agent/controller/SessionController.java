package com.essay.agent.controller;

import com.essay.agent.common.constant.ErrorCodeConstants;
import com.essay.agent.common.context.SessionContext;
import com.essay.agent.common.exception.BusinessException;
import com.essay.agent.model.dto.response.ApiResponse;
import com.essay.agent.service.ContextManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/session")
public class SessionController {

    @Autowired
    private ContextManager contextManager;

    @DeleteMapping("/context")
    public ApiResponse<Void> clearContext() {
        String sessionId = SessionContext.getSessionId();
        if (sessionId == null) {
            throw new BusinessException(ErrorCodeConstants.SESSION_INVALID, "会话无效");
        }

        contextManager.clearContext(sessionId);

        log.info("Context cleared successfully. sessionId={}", sessionId);

        return ApiResponse.success(null, "上下文已清空");
    }
}