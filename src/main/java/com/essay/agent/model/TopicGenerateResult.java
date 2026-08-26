package com.essay.agent.model;

import lombok.Data;

@Data
public class TopicGenerateResult {
    private String topicDescription;
    private String writingRequirements;
    private Integer wordCount;
    private String difficulty;
    private boolean degraded;
}