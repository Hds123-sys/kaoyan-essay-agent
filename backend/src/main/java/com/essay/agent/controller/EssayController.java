package com.essay.agent.controller;

import com.essay.agent.common.context.SessionContext;
import com.essay.agent.common.exception.BusinessException;
import com.essay.agent.model.dto.request.CorrectRequest;
import com.essay.agent.model.dto.response.ApiResponse;
import com.essay.agent.model.dto.response.CorrectResponse;
import com.essay.agent.service.CorrectService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api")
public class EssayController {

    @Autowired
    private CorrectService correctService;

    @PostMapping("/correct")
    public ApiResponse<CorrectResponse> correct(@Valid @RequestBody CorrectRequest request) {
        String sessionId = SessionContext.get();
        log.info("Received correct request - sessionId: {}, essayType: {}, topic: {}",
                sessionId, request.getEssayType(), request.getTopic());

        try {
            CorrectResponse response = correctService.correct(
                    request.getTopic(),
                    request.getUserEssay(),
                    request.getEssayType(),
                    request.getImageUrl(),
                    request.getTemplateId()
            );

            log.info("Correct request completed - sessionId: {}, recordId: {}", sessionId, response.getId());
            return ApiResponse.success(response);

        } catch (BusinessException e) {
            log.error("Correct request failed - sessionId: {}, error: {}", sessionId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Correct request error - sessionId: {}", sessionId, e);
            throw new BusinessException(500, "批改请求处理失败: " + e.getMessage());
        }
    }
}