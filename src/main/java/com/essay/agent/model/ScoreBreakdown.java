package com.essay.agent.model;

import lombok.Data;

@Data
public class ScoreBreakdown {
    private Integer content;
    private Integer language;
    private Integer vocabulary;
    private Integer structure;
}