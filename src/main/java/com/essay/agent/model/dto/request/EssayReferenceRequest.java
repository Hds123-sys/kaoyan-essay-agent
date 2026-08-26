package com.essay.agent.model.dto.request;

import com.essay.agent.model.EssayType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EssayReferenceRequest {
    private String sessionId;
    @NotBlank(message = "题目内容不能为空")
    private String topic;
    @NotNull(message = "作文类型不能为空")
    private EssayType essayType;
}