package com.essay.agent.model.dto.request;

import com.essay.agent.model.EssayType;
import lombok.Data;

@Data
public class EssayReferenceRequest {
    private String sessionId;
    private String topic;
    private EssayType essayType;
}