package com.essay.agent.model.dto.request;

import com.essay.agent.model.EssayType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EssayCorrectRequest {
    private EssayType essayType;
    private String topic;
    @NotBlank(message = "作文内容不能为空")
    private String userEssay;
    private String imageUrl;
    private Boolean isHeavilyEdited;
}