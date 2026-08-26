package com.essay.agent.model;

import lombok.Data;

@Data
public class ReferenceResult {
    private String referenceEssay;
    private Integer wordCount;
    private java.util.List<String> highlights;
    private boolean degraded;
}