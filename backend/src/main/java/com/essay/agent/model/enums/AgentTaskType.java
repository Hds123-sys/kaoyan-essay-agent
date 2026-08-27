package com.essay.agent.model.enums;

import lombok.Getter;

@Getter
public enum AgentTaskType {

    TOPIC("topic_generate"),
    REFERENCE("essay_reference"),
    CORRECT("essay_correct"),
    CORRECT_MAJOR("essay_correct_major"),
    CORRECT_LETTER("essay_correct_letter");

    private final String templateName;

    AgentTaskType(String templateName) {
        this.templateName = templateName;
    }

}