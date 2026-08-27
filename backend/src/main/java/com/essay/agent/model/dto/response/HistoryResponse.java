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
public class HistoryResponse {

    private Long id;

    private String essayType;

    private String topic;

    private String userEssay;

    private String imageUrl;

    private String templateVersion;

    private LocalDateTime createdAt;

    private String summary;
}