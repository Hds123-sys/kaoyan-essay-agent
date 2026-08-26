package com.essay.agent.model.dto.request;

import lombok.Data;

@Data
public class EssayCorrectRequest {
    private String sessionId;
    private String essayType;
    private String topic;
    private String userEssay;
    private String imageUrl;
    private Boolean isHeavilyEdited;
}