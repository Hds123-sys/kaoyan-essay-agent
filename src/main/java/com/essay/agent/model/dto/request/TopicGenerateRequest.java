package com.essay.agent.model.dto.request;

import com.essay.agent.model.EssayType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TopicGenerateRequest {
    private String sessionId;
    @NotNull(message = "作文类型不能为空")
    private EssayType essayType;
}