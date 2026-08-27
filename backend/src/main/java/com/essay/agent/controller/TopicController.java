package com.essay.agent.controller;

import com.essay.agent.common.context.SessionContext;
import com.essay.agent.common.exception.BusinessException;
import com.essay.agent.model.dto.request.TopicRequest;
import com.essay.agent.model.dto.response.ApiResponse;
import com.essay.agent.model.dto.response.TopicResponse;
import com.essay.agent.service.TopicService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api")
public class TopicController {

    @Autowired
    private TopicService topicService;

    @PostMapping("/generate-topic")
    public ApiResponse<List<TopicResponse>> generateTopics(@RequestBody TopicRequest request) {
        String sessionId = SessionContext.get();
        log.info("Generate topics request - sessionId: {}, essayType: {}, difficulty: {}",
                sessionId, request.getEssayType(), request.getDifficulty());

        try {
            List<TopicResponse> result = topicService.generateTopics(
                    request.getEssayType(),
                    request.getDifficulty(),
                    request.getKeywords()
            );

            log.info("Topics generated - sessionId: {}, count: {}", sessionId, result.size());
            return ApiResponse.success(result);

        } catch (BusinessException e) {
            log.error("Generate topics failed - sessionId: {}, error: {}", sessionId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Generate topics error - sessionId: {}", sessionId, e);
            throw new BusinessException(500, "生成题目失败: " + e.getMessage());
        }
    }
}