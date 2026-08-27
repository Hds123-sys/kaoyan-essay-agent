package com.essay.agent.controller;

import com.essay.agent.common.exception.BusinessException;
import com.essay.agent.model.dto.response.ApiResponse;
import com.essay.agent.model.dto.response.SessionResponse;
import com.essay.agent.service.SessionService;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/session")
public class SessionController {

    @Autowired
    private SessionService sessionService;

    @PostMapping
    public ApiResponse<SessionResponse> createSession() {
        log.info("Create session request");

        try {
            SessionResponse result = sessionService.createSession();
            log.info("Session created - sessionId: {}", result.getSessionId());
            return ApiResponse.success(result);

        } catch (Exception e) {
            log.error("Create session error", e);
            throw new BusinessException(500, "创建会话失败: " + e.getMessage());
        }
    }

    @GetMapping("/{sessionId}")
    public ApiResponse<SessionResponse> getSession(@PathVariable @NotBlank String sessionId) {
        log.info("Get session request - sessionId: {}", sessionId);

        try {
            SessionResponse result = sessionService.getSession(sessionId);
            return ApiResponse.success(result);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Get session error - sessionId: {}", sessionId, e);
            throw new BusinessException(500, "获取会话失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{sessionId}")
    public ApiResponse<Void> clearSession(@PathVariable @NotBlank String sessionId) {
        log.info("Clear session request - sessionId: {}", sessionId);

        try {
            sessionService.clearSession(sessionId);
            log.info("Session cleared - sessionId: {}", sessionId);
            return ApiResponse.success(null);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Clear session error - sessionId: {}", sessionId, e);
            throw new BusinessException(500, "清除会话失败: " + e.getMessage());
        }
    }
}