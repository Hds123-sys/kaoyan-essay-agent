package com.essay.agent.model.dto.request;

import jakarta.validation.constraints.NotBlank;

public class SanitizeRequest {

    @NotBlank(message = "markdown不能为空")
    private String markdown;

    public String getMarkdown() {
        return markdown;
    }

    public void setMarkdown(String markdown) {
        this.markdown = markdown;
    }
}