package com.essay.agent.model.dto.request;

import lombok.Data;

@Data
public class EssayReferenceRequest {
    private String sessionId;
    private String topic;
    private String essayType;
}