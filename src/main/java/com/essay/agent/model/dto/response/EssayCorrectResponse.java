package com.essay.agent.model.dto.response;

import com.essay.agent.model.EssayCorrectResult;
import lombok.Data;

@Data
public class EssayCorrectResponse {
    private EssayCorrectResult result;
    private boolean summaryDegraded;
    private String templateId;
    private String templateVersion;
    private Integer iterationCount;
}