package com.essay.agent.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OcrRequest {

    @NotBlank(message = "imageUrl不能为空")
    private String imageUrl;
}