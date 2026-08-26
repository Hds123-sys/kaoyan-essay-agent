package com.essay.agent.model;

import lombok.Data;

@Data
public class EssayCorrectResult {
    private Integer totalScore;
    private ScoreBreakdown breakdown;
    private java.util.List<ErrorItem> errors;
    private String weaknesses;
    private String polishedEssay;
    private java.util.List<String> advancedPhrases;
    private boolean degraded;
}