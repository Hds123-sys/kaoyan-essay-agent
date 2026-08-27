package com.essay.agent.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CorrectRequest {

    @NotBlank(message = "topic不能为空")
    private String topic;

    @NotBlank(message = "userEssay不能为空")
    private String userEssay;

    @NotBlank(message = "essayType不能为空")
    private String essayType;

    private String imageUrl;

    private String templateId;
}