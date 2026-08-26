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
public class ReferenceResult {

    private String referenceEssay;

    private Integer wordCount;

    private List<String> highlights;

    private boolean degraded;

}