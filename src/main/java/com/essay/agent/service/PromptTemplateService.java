package com.essay.agent.service;

import com.essay.agent.model.EssayType;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PromptTemplateService {

    public String getLockedTemplate(String sessionId, String taskType, String essayType) {
        return "template_" + taskType + "_" + essayType;
    }

    public String resolveTemplate(String templateId, String version, Map<String, Object> variables) {
        String template = getTemplateContent(templateId);
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            template = template.replace(placeholder, String.valueOf(entry.getValue()));
        }
        return template;
    }

    private String getTemplateContent(String templateId) {
        if (templateId.contains("CORRECT")) {
            return "请批改以下作文：\n" +
                   "作文类型：{{essay_type}}\n" +
                   "题目：{{topic}}\n" +
                   "内容：{{user_essay}}\n" +
                   "评分标准：{{scoring_criteria}}";
        } else if (templateId.contains("TOPIC")) {
            return "请生成一个{{essay_type}}类型的作文题目";
        } else {
            return "请参考以下作文类型写一篇参考作文：{{essay_type}}";
        }
    }
}