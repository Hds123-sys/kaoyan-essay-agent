package com.essay.agent.model.agent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EssayCorrectResult {

    private Integer totalScore;

    private ScoreBreakdown breakdown;

    private List<ErrorItem> errors;

    private String weaknesses;

    private String polishedEssay;

    private List<String> advancedPhrases;

    private boolean degraded;

}