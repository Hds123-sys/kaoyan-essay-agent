package com.essay.agent.model.dto.request;

import lombok.Data;

@Data
public class TopicRequest {

    private String essayType;

    private String difficulty;

    private String keywords;
}