package com.essay.agent.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CorrectResponse {

    private Long id;

    private String sessionId;

    private String essayType;

    private String topic;

    private String userEssay;

    private String resultJson;

    private String imageUrl;

    private String templateId;

    private String templateVersion;

    private LocalDateTime createdAt;
}