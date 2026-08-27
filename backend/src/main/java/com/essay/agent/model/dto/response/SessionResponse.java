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
public class SessionResponse {

    private String sessionId;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime lastActiveAt;

    private Integer roundCount;

    private String currentTopic;
}