package com.essay.agent.model.agent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopicGenerateResult {

    private String topicDescription;

    private String writingRequirements;

    private Integer wordCount;

    private String difficulty;

    private boolean degraded;

}