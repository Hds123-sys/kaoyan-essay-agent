package com.essay.agent.service;

import com.essay.agent.model.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ResponseParser {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public EssayCorrectResult parseCorrectResult(String rawResponse) {
        EssayCorrectResult result = new EssayCorrectResult();
        result.setDegraded(false);

        try {
            result = objectMapper.readValue(rawResponse, EssayCorrectResult.class);
        } catch (Exception e) {
            String extractedJson = extractJsonFromMarkdown(rawResponse);
            if (extractedJson != null) {
                try {
                    result = objectMapper.readValue(extractedJson, EssayCorrectResult.class);
                } catch (Exception ex) {
                    result = createDegradedCorrectResult(rawResponse);
                }
            } else {
                result = createDegradedCorrectResult(rawResponse);
            }
        }
        return result;
    }

    public TopicGenerateResult parseTopicResult(String rawResponse) {
        TopicGenerateResult result = new TopicGenerateResult();

        try {
            result = objectMapper.readValue(rawResponse, TopicGenerateResult.class);
        } catch (Exception e) {
            String extractedJson = extractJsonFromMarkdown(rawResponse);
            if (extractedJson != null) {
                try {
                    result = objectMapper.readValue(extractedJson, TopicGenerateResult.class);
                } catch (Exception ex) {
                    result.setDegraded(true);
                }
            } else {
                result.setDegraded(true);
            }
        }
        return result;
    }

    public ReferenceResult parseReferenceResult(String rawResponse) {
        ReferenceResult result = new ReferenceResult();

        try {
            result = objectMapper.readValue(rawResponse, ReferenceResult.class);
        } catch (Exception e) {
            String extractedJson = extractJsonFromMarkdown(rawResponse);
            if (extractedJson != null) {
                result = objectMapper.readValue(extractedJson, ReferenceResult.class);
            }
        }
        return result;
    }

    public boolean isValidJson(String text) {
        try {
            objectMapper.readTree(text);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String extractJsonFromMarkdown(String text) {
        Pattern jsonCodeBlock = Pattern.compile("```json\\s*([\\s\\S]*?)\\s*```");
        Matcher matcher = jsonCodeBlock.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        Pattern firstBraceToLast = Pattern.compile("\\{[\\s\\S]*\\}");
        matcher = firstBraceToLast.matcher(text);
        if (matcher.find()) {
            return matcher.group(0);
        }

        return null;
    }

    private EssayCorrectResult createDegradedCorrectResult(String originalEssay) {
        EssayCorrectResult result = new EssayCorrectResult();
        result.setDegraded(true);
        result.setTotalScore(null);
        result.setWeaknesses("格式解析失败，请重试");
        result.setPolishedEssay(originalEssay);
        return result;
    }
}