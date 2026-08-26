package com.essay.agent.model.dto.request;

import com.essay.agent.model.EssayType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReCorrectRequest {
    @NotNull(message = "记录ID不能为空")
    private Long recordId;
    private String topic;
    private String userEssay;
    private EssayType essayType;
}