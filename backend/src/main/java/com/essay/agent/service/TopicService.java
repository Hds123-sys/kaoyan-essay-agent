package com.essay.agent.service;

import com.essay.agent.common.context.SessionContext;
import com.essay.agent.common.exception.BusinessException;
import com.essay.agent.model.agent.ClaudeResponse;
import com.essay.agent.model.agent.Message;
import com.essay.agent.model.agent.TopicGenerateResult;
import com.essay.agent.model.dto.response.TopicResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class TopicService {

    @Autowired
    private ClaudeClient claudeClient;

    @Autowired
    private ResponseParser responseParser;

    @Value("${claude.max-tokens:1024}")
    private int maxTokens;

    @Value("${claude.temperature:0.8}")
    private double temperature;

    private static final String SYSTEM_PROMPT = "你是一个考研英语作文题目生成助手。请根据用户提供的条件（作文类型、难度、关键词）生成3个合适的作文题目。";

    public List<TopicResponse> generateTopics(String essayType, String difficulty, String keywords) {
        String sessionId = SessionContext.get();

        String userPrompt = buildUserPrompt(essayType, difficulty, keywords);

        try {
            ClaudeResponse response = claudeClient.chatWithRetry(
                    SYSTEM_PROMPT,
                    List.of(Message.builder()
                            .role("user")
                            .content(userPrompt)
                            .build()),
                    temperature,
                    maxTokens
            );

            String responseText = claudeClient.extractText(response);
            TopicGenerateResult result = responseParser.parseTopicResult(responseText);

            if (result == null) {
                throw new BusinessException(500, "生成题目失败");
            }

            return Arrays.asList(
                    TopicResponse.builder()
                            .topic(result.getTopicDescription())
                            .description(result.getWritingRequirements())
                            .keywords(keywords != null ? Arrays.asList(keywords.split(",")) : List.of())
                            .category(essayType)
                            .difficulty(getDifficulty(result))
                            .build()
            );

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Generate topics error - sessionId: {}", sessionId, e);
            throw new BusinessException(500, "生成题目失败: " + e.getMessage());
        }
    }

    private Integer getDifficulty(TopicGenerateResult result) {
        if (result.getDifficulty() != null) {
            return result.getDifficulty();
        }
        if (result.getDifficultyLevel() != null) {
            return parseDifficulty(result.getDifficultyLevel());
        }
        return 3;
    }

    private Integer parseDifficulty(String difficultyStr) {
        if (difficultyStr == null || difficultyStr.isEmpty()) {
            return 3;
        }

        try {
            return Integer.parseInt(difficultyStr);
        } catch (NumberFormatException e) {
            switch (difficultyStr.toLowerCase()) {
                case "easy": return 1;
                case "medium": return 3;
                case "hard": return 5;
                default: return 3;
            }
        }
    }

    private String buildUserPrompt(String essayType, String difficulty, String keywords) {
        StringBuilder sb = new StringBuilder();
        sb.append("请生成3个考研英语作文题目。");

        if (essayType != null && !essayType.isEmpty()) {
            sb.append("作文类型：").append(essayType).append("。");
        }

        if (difficulty != null && !difficulty.isEmpty()) {
            sb.append("难度：").append(difficulty).append("。");
        }

        if (keywords != null && !keywords.isEmpty()) {
            sb.append("关键词：").append(keywords).append("。");
        }

        sb.append("请以JSON格式返回，包含topicDescription（题目描述）、writingRequirements（写作要求）、wordCount（字数要求）、difficulty（难度等级1-5）。");

        return sb.toString();
    }
}