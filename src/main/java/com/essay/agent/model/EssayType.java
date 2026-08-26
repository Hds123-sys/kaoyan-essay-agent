package com.essay.agent.model;

public enum EssayType {
    ACADEMIC("学术作文"),
    CREATIVE("创意作文"),
    PROFESSIONAL("职业写作"),
    GENERAL("普通作文");

    private final String displayName;

    EssayType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getScoringCriteria() {
        return switch (this) {
            case ACADEMIC -> "学术性、逻辑性、引用准确性";
            case CREATIVE -> "创意性、表达力、想象力";
            case PROFESSIONAL -> "专业性、清晰度、实用性";
            case GENERAL -> "完整性、流畅度、规范性";
        };
    }
}