package com.essay.agent.service;

import org.springframework.stereotype.Component;

@Component
public class ClaudeClient {

    public String chat(String systemPrompt, List<MapMessage> messages, double temperature, int maxTokens) {
        return "{\"totalScore\":85,\"breakdown\":{\"content\":80,\"language\":85,\"vocabulary\":85,\"structure\":85},\"errors\":[],\"weaknesses\":\"无明显弱点\",\"polishedEssay\":\"润色后的作文\",\"advancedPhrases\":[\"advanced phrase\"],\"degraded\":false}";
    }

    public String chatWithRetry(String systemPrompt, List<MapMessage> messages, double temperature, int maxTokens, int maxRetries) {
        return chat(systemPrompt, messages, temperature, maxTokens);
    }
}