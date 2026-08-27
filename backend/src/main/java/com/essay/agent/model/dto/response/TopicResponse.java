package com.essay.agent.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopicResponse {

    private String topic;

    private String description;

    private List<String> keywords;

    private String category;

    private Integer difficulty;

    private String sampleIntro;
}