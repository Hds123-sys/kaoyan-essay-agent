package com.essay.agent.model.dto.request;

import lombok.Data;

@Data
public class TopicGenerateRequest {
    private String sessionId;
    private String essayType;
}