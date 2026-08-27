package com.essay.agent.service;

import cn.hutool.json.JSONUtil;
import com.essay.agent.common.context.SessionContext;
import com.essay.agent.common.exception.BusinessException;
import com.essay.agent.common.util.WordCounter;
import com.essay.agent.common.util.LanguageDetector;
import com.essay.agent.mapper.EssayRecordMapper;
import com.essay.agent.model.agent.ClaudeResponse;
import com.essay.agent.model.agent.EssayCorrectResult;
import com.essay.agent.model.agent.Message;
import com.essay.agent.model.dto.response.CorrectResponse;
import com.essay.agent.model.enums.AgentTaskType;
import com.essay.agent.model.enums.EssayType;
import com.essay.agent.model.entity.EssayRecord;
import com.essay.agent.service.PromptTemplateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class CorrectService {

    @Autowired
    private ClaudeClient claudeClient;

    @Autowired
    private PromptTemplateService promptTemplateService;

    @Autowired
    private EssayRecordMapper essayRecordMapper;

    @Autowired
    private ContextManager contextManager;

    @Autowired
    private ResponseParser responseParser;

    @Value("${claude.max-tokens:2048}")
    private int maxTokens;

    @Value("${claude.temperature:0.7}")
    private double temperature;

    @Transactional
    public CorrectResponse correct(String topic, String userEssay, String essayType, String imageUrl, String templateId) {
        String sessionId = SessionContext.get();

        // 参数校验
        if (userEssay == null || userEssay.trim().isEmpty()) {
            throw new BusinessException(400, "作文内容不能为空");
        }

        // 字数校验
        int wordCount = WordCounter.count(userEssay);
        if (wordCount < 50) {
            throw new BusinessException(400, "作文字数不能少于50字");
        }

        // 语言检测
        String language = LanguageDetector.detect(userEssay);
        if (!"en".equals(language)) {
            throw new BusinessException(400, "目前只支持英语作文批改");
        }

        // 构建系统提示词
        String systemPrompt = "你是一个专业的考研英语作文批改助手，请对学生提交的英语作文进行详细的批改和点评。";

        // 构建用户消息
        String userMessage = buildUserMessage(topic, userEssay, essayType);

        // 添加到上下文管理器
        contextManager.addMessage(sessionId, Message.builder()
                .role("user")
                .content(userMessage)
                .build());

        // 获取历史上下文
        List<Message> messages = contextManager.getMessages(sessionId, maxTokens);

        // 调用Claude API
        ClaudeResponse claudeResponse = claudeClient.chatWithRetry(systemPrompt, messages, temperature, maxTokens);

        // 解析响应
        String responseText = claudeClient.extractText(claudeResponse);
        EssayCorrectResult result = responseParser.parseCorrectResult(responseText, userEssay);

        // 保存记录
        EssayRecord record = EssayRecord.builder()
                .sessionId(sessionId)
                .essayType(essayType)
                .topic(topic)
                .userEssay(userEssay)
                .resultJson(JSONUtil.toJsonStr(result))
                .imageUrl(imageUrl)
                .templateId(templateId)
                .templateVersion(promptTemplateService.getCurrentVersion())
                .userDisputed(0)
                .isHeavilyEdited(0)
                .createdAt(LocalDateTime.now())
                .build();

        essayRecordMapper.insert(record);

        // 添加助手响应到上下文
        contextManager.addMessage(sessionId, Message.builder()
                .role("assistant")
                .content(responseText)
                .build());

        // 构建响应
        return CorrectResponse.builder()
                .id(record.getId())
                .sessionId(sessionId)
                .essayType(essayType)
                .topic(topic)
                .userEssay(userEssay)
                .resultJson(JSONUtil.toJsonStr(result))
                .imageUrl(imageUrl)
                .templateId(templateId)
                .templateVersion(record.getTemplateVersion())
                .createdAt(record.getCreatedAt())
                .build();
    }

    private String buildUserMessage(String topic, String userEssay, String essayType) {
        StringBuilder sb = new StringBuilder();
        sb.append("请批改以下作文：\n\n");
        sb.append("作文类型：").append(essayType).append("\n");
        sb.append("题目：").append(topic).append("\n\n");
        sb.append("作文内容：\n").append(userEssay);
        return sb.toString();
    }
}