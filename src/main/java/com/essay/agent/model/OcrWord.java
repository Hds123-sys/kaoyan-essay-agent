package com.essay.agent.model;

import lombok.Data;

@Data
public class OcrWord {
    private String word;
    private Double confidence;
}