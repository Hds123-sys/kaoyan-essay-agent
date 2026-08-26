package com.essay.agent.model;

import lombok.Data;

import java.util.List;

@Data
public class OcrResult {
    private String ocrText;
    private String imageUrl;
    private Double averageConfidence;
    private List<OcrWord> words;
    private String warning;
}