package com.essay.agent.controller;

import com.essay.agent.common.context.SessionContext;
import com.essay.agent.common.exception.BusinessException;
import com.essay.agent.common.util.MarkdownSanitizer;
import com.essay.agent.model.dto.request.SanitizeRequest;
import com.essay.agent.model.dto.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/report")
public class ReportController {

    @PostMapping("/sanitize")
    public ApiResponse<Map<String, String>> sanitize(@Valid @RequestBody SanitizeRequest request) {
        String sessionId = SessionContext.get();
        log.debug("Sanitize request from session: {}", sessionId);

        if (request.getMarkdown() == null || request.getMarkdown().trim().isEmpty()) {
            throw new BusinessException(400, "markdown不能为空");
        }

        String sanitized = MarkdownSanitizer.sanitize(request.getMarkdown());
        log.debug("Sanitized markdown for session: {}", sessionId);

        return ApiResponse.success(Map.of("sanitized_markdown", sanitized));
    }
}