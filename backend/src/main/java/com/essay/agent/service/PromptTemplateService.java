package com.essay.agent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.essay.agent.common.constant.RedisKeyConstants;
import com.essay.agent.common.util.RedisUtil;
import com.essay.agent.model.agent.TemplateVersion;
import com.essay.agent.model.entity.PromptTemplate;
import com.essay.agent.model.enums.AgentTaskType;
import com.essay.agent.model.enums.EssayType;
import com.essay.agent.mapper.PromptTemplateMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class PromptTemplateService {

    @Autowired
    private PromptTemplateMapper templateMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Value("${agent.template.gray-percentage:20}")
    private int grayPercentage;

    private static final long LOCK_TTL_DAYS = 7;
    private static final long CACHE_TTL_HOURS = 1;
    private static final Pattern CONDITION_BLOCK_PATTERN = Pattern.compile("\\{\\{(\\w+)\\}\\}.*?\\{\\{/\\1\\}\\}", Pattern.DOTALL);
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{(\\w+)\\}\\}");

    public TemplateVersion getLockedTemplate(String sessionId, AgentTaskType taskType, EssayType essayType) {
        String lockKey = RedisKeyConstants.buildTemplateLockKey(sessionId);
        TemplateVersion cached = redisUtil.get(lockKey, TemplateVersion.class);

        if (cached != null) {
            log.debug("Using locked template - sessionId: {}, template: {}, version: {}",
                    sessionId, cached.getTemplateName(), cached.getVersion());
            return cached;
        }

        TemplateVersion template;
        String templateName = getTemplateNameForTask(taskType, essayType);

        if (shouldUseNewVersion(sessionId)) {
            template = getLatestVersion(templateName);
            log.info("New version assigned - sessionId: {}, template: {}, version: {}",
                    sessionId, templateName, template.getVersion());
        } else {
            template = getStableVersion(templateName);
            log.info("Stable version assigned - sessionId: {}, template: {}, version: {}",
                    sessionId, templateName, template.getVersion());
        }

        redisUtil.set(lockKey, template, LOCK_TTL_DAYS, TimeUnit.DAYS);
        return template;
    }

    public String resolveTemplate(String templateId, String version, Map<String, String> variables) {
        String content = getTemplateContent(templateId, version);

        content = processConditionBlocks(content, variables);
        content = replaceVariables(content, variables);

        return content;
    }

    private String getTemplateContent(String templateId, String version) {
        String cacheKey = RedisKeyConstants.buildTemplateCacheKey(templateId, version);
        String cached = redisUtil.get(cacheKey, String.class);

        if (cached != null) {
            return cached;
        }

        LambdaQueryWrapper<PromptTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PromptTemplate::getTemplateName, templateId)
                .eq(PromptTemplate::getVersion, version)
                .eq(PromptTemplate::getEnabled, 1)
                .last("LIMIT 1");

        PromptTemplate template = templateMapper.selectOne(wrapper);
        if (template == null) {
            throw new RuntimeException("模板不存在: " + templateId + " v" + version);
        }

        redisUtil.set(cacheKey, template.getContent(), CACHE_TTL_HOURS, TimeUnit.HOURS);
        return template.getContent();
    }

    private String processConditionBlocks(String content, Map<String, String> variables) {
        Matcher matcher = CONDITION_BLOCK_PATTERN.matcher(content);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String varName = matcher.group(1);
            String varValue = variables.get(varName);

            if (varValue == null || varValue.trim().isEmpty()) {
                matcher.appendReplacement(result, "");
            } else {
                matcher.appendReplacement(result, matcher.group());
            }
        }
        matcher.appendTail(result);

        return result.toString();
    }

    private String replaceVariables(String content, Map<String, String> variables) {
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            String value = entry.getValue() != null ? entry.getValue() : "";
            content = content.replace(placeholder, value);
        }
        return content;
    }

    private TemplateVersion getLatestVersion(String templateName) {
        LambdaQueryWrapper<PromptTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PromptTemplate::getTemplateName, templateName)
                .eq(PromptTemplate::getEnabled, 1)
                .orderByDesc(PromptTemplate::getVersion)
                .last("LIMIT 1");

        PromptTemplate template = templateMapper.selectOne(wrapper);
        return convertToTemplateVersion(template);
    }

    private TemplateVersion getStableVersion(String templateName) {
        LambdaQueryWrapper<PromptTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PromptTemplate::getTemplateName, templateName)
                .eq(PromptTemplate::getEnabled, 1)
                .notLike(PromptTemplate::getVersion, "beta")
                .notLike(PromptTemplate::getVersion, "rc")
                .orderByDesc(PromptTemplate::getVersion)
                .last("LIMIT 1");

        PromptTemplate template = templateMapper.selectOne(wrapper);
        return convertToTemplateVersion(template);
    }

    private boolean shouldUseNewVersion(String sessionId) {
        int hash = Math.abs(sessionId.hashCode());
        return (hash % 100) < grayPercentage;
    }

    private String getTemplateNameForTask(AgentTaskType taskType, EssayType essayType) {
        if (taskType == AgentTaskType.CORRECT_MAJOR) {
            return AgentTaskType.CORRECT_MAJOR.getTemplateName();
        } else if (taskType == AgentTaskType.CORRECT_LETTER) {
            return AgentTaskType.CORRECT_LETTER.getTemplateName();
        }
        return taskType.getTemplateName();
    }

    private TemplateVersion convertToTemplateVersion(PromptTemplate template) {
        return TemplateVersion.builder()
                .templateId(String.valueOf(template.getId()))
                .version(template.getVersion())
                .templateName(template.getTemplateName())
                .essayType(template.getEssayType())
                .temperature(template.getTemperature() != null ? template.getTemperature().doubleValue() : 0.3)
                .content(template.getContent())
                .build();
    }

    public String getCurrentVersion() {
        return "v1.0";
    }

    public String getSystemPrompt(AgentTaskType taskType) {
        return "你是一个专业的考研英语作文批改助手，请对学生提交的英语作文进行详细的批改和点评。";
    }
}