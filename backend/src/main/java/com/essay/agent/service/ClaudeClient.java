package com.essay.agent.service;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.essay.agent.common.context.SessionContext;
import com.essay.agent.common.constant.ErrorCodeConstants;
import com.essay.agent.common.exception.BusinessException;
import com.essay.agent.model.agent.ClaudeRequest;
import com.essay.agent.model.agent.ClaudeResponse;
import com.essay.agent.model.agent.Message;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class ClaudeClient {

    @Value("${claude.api-key}")
    private String apiKey;

    @Value("${claude.base-url}")
    private String baseUrl;

    @Value("${claude.model}")
    private String model;

    @Value("${claude.timeout-seconds:30}")
    private int timeoutSeconds;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String API_VERSION = "2023-06-01";
    private static final int MAX_HTTP_RETRIES = 2;

    public ClaudeResponse chat(String systemPrompt, List<Message> messages, double temperature, int maxTokens) {
        String sessionId = SessionContext.get();

        ClaudeRequest request = ClaudeRequest.builder()
                .model(model)
                .maxTokens(maxTokens)
                .temperature(temperature)
                .messages(messages)
                .system(systemPrompt)
                .build();

        String url = baseUrl.endsWith("/") ? baseUrl + "v1/messages" : baseUrl + "/v1/messages";
        String requestBody;

        try {
            requestBody = objectMapper.writeValueAsString(request);
        } catch (Exception e) {
            throw new BusinessException(ErrorCodeConstants.CLAUDE_API_ERROR, "请求序列化失败");
        }

        int retryCount = 0;
        long startTime = System.currentTimeMillis();

        while (retryCount <= MAX_HTTP_RETRIES) {
            try {
                HttpResponse response = HttpRequest.post(url)
                        .header("x-api-key", apiKey)
                        .header("anthropic-version", API_VERSION)
                        .header("Content-Type", "application/json")
                        .timeout(timeoutSeconds * 1000)
                        .body(requestBody)
                        .execute();

                int statusCode = response.getStatus();

                if (statusCode >= 200 && statusCode < 300) {
                    String responseBody = response.body();
                    ClaudeResponse claudeResponse = objectMapper.readValue(responseBody, ClaudeResponse.class);

                    long duration = System.currentTimeMillis() - startTime;
                    int inputTokens = claudeResponse.getUsage() != null ? claudeResponse.getUsage().getInputTokens() : 0;
                    int outputTokens = claudeResponse.getUsage() != null ? claudeResponse.getUsage().getOutputTokens() : 0;

                    log.info("Claude API call success - sessionId: {}, duration: {}ms, inputTokens: {}, outputTokens: {}, statusCode: {}",
                            sessionId, duration, inputTokens, outputTokens, statusCode);

                    return claudeResponse;
                }

                if (statusCode >= 500 || statusCode == 429) {
                    retryCount++;
                    if (retryCount <= MAX_HTTP_RETRIES) {
                        long delay = (long) Math.pow(2, retryCount - 1) * 1000;
                        log.warn("Claude API call failed, retrying - sessionId: {}, statusCode: {}, retryCount: {}, delay: {}ms",
                                sessionId, statusCode, retryCount, delay);
                        TimeUnit.MILLISECONDS.sleep(delay);
                        continue;
                    }
                }

                throw new BusinessException(ErrorCodeConstants.CLAUDE_API_ERROR,
                        "Claude API调用失败, statusCode: " + statusCode);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new BusinessException(ErrorCodeConstants.CLAUDE_API_ERROR, "请求被中断");
            } catch (BusinessException e) {
                throw e;
            } catch (Exception e) {
                retryCount++;
                if (retryCount <= MAX_HTTP_RETRIES) {
                    long delay = (long) Math.pow(2, retryCount - 1) * 1000;
                    log.warn("Claude API call failed, retrying - sessionId: {}, error: {}, retryCount: {}, delay: {}ms",
                            sessionId, e.getMessage(), retryCount, delay);
                    try {
                        TimeUnit.MILLISECONDS.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new BusinessException(ErrorCodeConstants.CLAUDE_API_ERROR, "请求被中断");
                    }
                    continue;
                }
                throw new BusinessException(ErrorCodeConstants.CLAUDE_API_ERROR, "Claude API调用失败: " + e.getMessage());
            }
        }

        throw new BusinessException(ErrorCodeConstants.CLAUDE_API_ERROR, "Claude API调用失败，重试耗尽");
    }

    public ClaudeResponse chatWithRetry(String systemPrompt, List<Message> messages, double temperature, int maxTokens) {
        return chat(systemPrompt, messages, temperature, maxTokens);
    }

    public String extractText(ClaudeResponse response) {
        if (response == null || response.getContent() == null) {
            return "";
        }

        return response.getContent().stream()
                .filter(block -> "text".equals(block.getType()))
                .map(ClaudeResponse.ContentBlock::getText)
                .reduce("", (a, b) -> a + b);
    }

}