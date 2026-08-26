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
public class ScoreBreakdown {

    private Integer content;

    private Integer language;

    private Integer vocabulary;

    private Integer structure;

}