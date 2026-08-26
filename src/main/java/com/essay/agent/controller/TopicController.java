package com.essay.agent.controller;

import com.essay.agent.common.constant.ErrorCodeConstants;
import com.essay.agent.common.context.SessionContext;
import com.essay.agent.common.exception.BusinessException;
import com.essay.agent.model.TopicGenerateResult;
import com.essay.agent.model.dto.request.TopicGenerateRequest;
import com.essay.agent.model.dto.response.ApiResponse;
import com.essay.agent.service.AgentDispatcher;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/topic")
public class TopicController {

    @Autowired
    private AgentDispatcher agentDispatcher;

    @PostMapping("/generate")
    public ApiResponse<TopicGenerateResult> generate(@Valid @RequestBody TopicGenerateRequest request) {
        String sessionId = SessionContext.getSessionId();
        if (sessionId == null) {
            throw new BusinessException(ErrorCodeConstants.SESSION_INVALID, "会话无效");
        }

        if (request.getEssayType() == null) {
            throw new BusinessException(ErrorCodeConstants.ESSAY_TYPE_REQUIRED, "作文类型不能为空");
        }

        request.setSessionId(sessionId);

        TopicGenerateResult result = agentDispatcher.generateTopic(request);

        log.info("Topic generation completed successfully. sessionId={}, essayType={}, degraded={}",
                sessionId, request.getEssayType(), result.isDegraded());

        return ApiResponse.success(result);
    }
}