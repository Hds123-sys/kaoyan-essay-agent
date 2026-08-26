package com.essay.agent.service;

import com.essay.agent.model.agent.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class ResponseParser {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final Pattern JSON_CODE_BLOCK_PATTERN = Pattern.compile("```json\\s*([\\s\\S]*?)\\s*```");
    private static final Pattern BRACE_PATTERN = Pattern.compile("\\{[\\s\\S]*\\}");

    public EssayCorrectResult parseCorrectResult(String rawResponse, String originalEssay) {
        if (rawResponse == null || rawResponse.trim().isEmpty()) {
            return createDegradedCorrectResult(originalEssay, "响应为空");
        }

        String jsonText = extractJsonFromMarkdown(rawResponse);
        if (jsonText == null) {
            jsonText = rawResponse;
        }

        try {
            return objectMapper.readValue(jsonText, EssayCorrectResult.class);
        } catch (Exception e) {
            log.warn("Failed to parse correct result as JSON, trying fallback", e);

            jsonText = extractFirstBraceBlock(rawResponse);
            if (jsonText != null) {
                try {
                    return objectMapper.readValue(jsonText, EssayCorrectResult.class);
                } catch (Exception ex) {
                    log.warn("Fallback parse also failed", ex);
                }
            }

            return createDegradedCorrectResult(originalEssay, "格式解析失败，请重试");
        }
    }

    public TopicGenerateResult parseTopicResult(String rawResponse) {
        if (rawResponse == null || rawResponse.trim().isEmpty()) {
            return createDegradedTopicResult("响应为空");
        }

        String jsonText = extractJsonFromMarkdown(rawResponse);
        if (jsonText == null) {
            jsonText = rawResponse;
        }

        try {
            return objectMapper.readValue(jsonText, TopicGenerateResult.class);
        } catch (Exception e) {
            log.warn("Failed to parse topic result as JSON, trying fallback", e);

            jsonText = extractFirstBraceBlock(rawResponse);
            if (jsonText != null) {
                try {
                    return objectMapper.readValue(jsonText, TopicGenerateResult.class);
                } catch (Exception ex) {
                    log.warn("Fallback parse also failed", ex);
                }
            }

            return createDegradedTopicResult("格式解析失败，请重试");
        }
    }

    public ReferenceResult parseReferenceResult(String rawResponse) {
        if (rawResponse == null || rawResponse.trim().isEmpty()) {
            return createDegradedReferenceResult("响应为空");
        }

        String jsonText = extractJsonFromMarkdown(rawResponse);
        if (jsonText == null) {
            jsonText = rawResponse;
        }

        try {
            return objectMapper.readValue(jsonText, ReferenceResult.class);
        } catch (Exception e) {
            log.warn("Failed to parse reference result as JSON, trying fallback", e);

            jsonText = extractFirstBraceBlock(rawResponse);
            if (jsonText != null) {
                try {
                    return objectMapper.readValue(jsonText, ReferenceResult.class);
                } catch (Exception ex) {
                    log.warn("Fallback parse also failed", ex);
                }
            }

            return createDegradedReferenceResult("格式解析失败，请重试");
        }
    }

    public boolean isValidJson(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }

        try {
            objectMapper.readTree(text);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String extractJsonFromMarkdown(String text) {
        Matcher matcher = JSON_CODE_BLOCK_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }

    private String extractFirstBraceBlock(String text) {
        Matcher matcher = BRACE_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    private EssayCorrectResult createDegradedCorrectResult(String originalEssay, String errorMessage) {
        return EssayCorrectResult.builder()
                .totalScore(null)
                .weaknesses(errorMessage)
                .polishedEssay(originalEssay)
                .degraded(true)
                .build();
    }

    private TopicGenerateResult createDegradedTopicResult(String errorMessage) {
        return TopicGenerateResult.builder()
                .topicDescription(errorMessage)
                .degraded(true)
                .build();
    }

    private ReferenceResult createDegradedReferenceResult(String errorMessage) {
        return ReferenceResult.builder()
                .referenceEssay(errorMessage)
                .degraded(true)
                .build();
    }

}