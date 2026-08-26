package com.essay.agent.service;

import com.essay.agent.model.EssayCorrectResult;
import com.essay.agent.model.EssayType;
import com.essay.agent.model.TopicGenerateResult;
import com.essay.agent.model.ReferenceResult;
import com.essay.agent.model.dto.request.EssayCorrectRequest;
import com.essay.agent.model.dto.request.TopicGenerateRequest;
import com.essay.agent.model.dto.request.EssayReferenceRequest;
import com.essay.agent.model.dto.response.EssayCorrectResponse;
import com.essay.agent.entity.EssayRecord;
import com.essay.agent.service.impl.HistoryServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AgentDispatcher {

    private static final String CORRECT = "CORRECT";
    private static final double DEFAULT_TEMPERATURE = 0.7;
    private static final int DEFAULT_MAX_TOKENS = 4000;
    private static final int RETRY_TEMPERATURE = 0.1;

    @Autowired
    private PromptTemplateService templateService;

    @Autowired
    private ContextManager contextManager;

    @Autowired
    private ClaudeClient claudeClient;

    @Autowired
    private ResponseParser responseParser;

    @Autowired
    private HistoryServiceImpl historyService;

    public EssayCorrectResponse correct(EssayCorrectRequest request) {
        String sessionId = request.getSessionId();
        String essayTypeStr = request.getEssayType();
        EssayType essayType = EssayType.valueOf(essayTypeStr.toUpperCase());

        long startTime = System.currentTimeMillis();
        int iterationCount = 0;
        boolean summaryDegraded = false;

        try {
            String templateId = templateService.getLockedTemplate(sessionId, CORRECT, essayTypeStr);
            String version = "1.0";

            List<MapMessage> truncatedContext = contextManager.getTruncatedContext(sessionId);

            Map<String, Object> variables = new HashMap<>();
            variables.put("essay_type", essayType.getDisplayName());
            variables.put("topic", request.getTopic());
            variables.put("user_essay", request.getUserEssay());
            variables.put("history_summary", null);
            variables.put("scoring_criteria", essayType.getScoringCriteria());

            String resolvedPrompt = templateService.resolveTemplate(templateId, version, variables);

            List<MapMessage> messages = new ArrayList<>(truncatedContext);
            MapMessage userMessage = new MapMessage();
            userMessage.setRole("user");
            userMessage.setContent(resolvedPrompt);
            messages.add(userMessage);

            String systemPrompt = "You are an essay correction assistant.";
            iterationCount++;

            String rawResponse = claudeClient.chat(systemPrompt, messages, DEFAULT_TEMPERATURE, DEFAULT_MAX_TOKENS);

            if (!responseParser.isValidJson(rawResponse)) {
                log.info("JSON validation failed, retrying with lower temperature. sessionId={}", sessionId);
                rawResponse = claudeClient.chatWithRetry(systemPrompt, messages, RETRY_TEMPERATURE, DEFAULT_MAX_TOKENS, 1);
                iterationCount++;
            }

            EssayCorrectResult result = responseParser.parseCorrectResult(rawResponse);

            contextManager.appendRound(sessionId, resolvedPrompt, rawResponse);

            EssayRecord record = new EssayRecord();
            record.setSessionId(sessionId);
            record.setEssayType(essayTypeStr);
            record.setTopic(request.getTopic());
            record.setUserEssay(request.getUserEssay());
            record.setTotalScore(result.getTotalScore());
            record.setPolishedEssay(result.getPolishedEssay());
            record.setWeaknesses(result.getWeaknesses());
            record.setCreatedAt(LocalDateTime.now());
            record.setUpdatedAt(LocalDateTime.now());
            historyService.saveRecord(record);

            long durationMs = System.currentTimeMillis() - startTime;
            log.info("Essay correction completed. sessionId={}, taskType={}, templateId={}, templateVersion={}, iterationCount={}, durationMs={}, success={}",
                    sessionId, CORRECT, templateId, version, iterationCount, durationMs, !result.isDegraded());

            EssayCorrectResponse response = new EssayCorrectResponse();
            response.setResult(result);
            response.setSummaryDegraded(summaryDegraded);
            response.setTemplateId(templateId);
            response.setTemplateVersion(version);
            response.setIterationCount(iterationCount);
            return response;

        } catch (Exception e) {
            log.error("Essay correction failed. sessionId={}", sessionId, e);

            EssayCorrectResult degradedResult = new EssayCorrectResult();
            degradedResult.setDegraded(true);
            degradedResult.setTotalScore(null);
            degradedResult.setWeaknesses("批改失败：" + e.getMessage());
            degradedResult.setPolishedEssay(request.getUserEssay());

            long durationMs = System.currentTimeMillis() - startTime;
            log.info("Essay correction failed (degraded). sessionId={}, taskType={}, durationMs={}, success=false",
                    sessionId, CORRECT, durationMs);

            EssayCorrectResponse response = new EssayCorrectResponse();
            response.setResult(degradedResult);
            response.setSummaryDegraded(true);
            response.setIterationCount(iterationCount);
            return response;
        }
    }

    public TopicGenerateResult generateTopic(TopicGenerateRequest request) {
        String sessionId = request.getSessionId();
        String essayTypeStr = request.getEssayType();

        long startTime = System.currentTimeMillis();

        try {
            String templateId = templateService.getLockedTemplate(sessionId, "TOPIC", essayTypeStr);
            String version = "1.0";

            Map<String, Object> variables = new HashMap<>();
            variables.put("essay_type", essayTypeStr);

            String resolvedPrompt = templateService.resolveTemplate(templateId, version, variables);

            List<MapMessage> messages = new ArrayList<>();
            MapMessage userMessage = new MapMessage();
            userMessage.setRole("user");
            userMessage.setContent(resolvedPrompt);
            messages.add(userMessage);

            String systemPrompt = "You are a topic generation assistant.";
            String rawResponse = claudeClient.chat(systemPrompt, messages, DEFAULT_TEMPERATURE, DEFAULT_MAX_TOKENS);

            TopicGenerateResult result = responseParser.parseTopicResult(rawResponse);

            MapMessage systemMsg = new MapMessage();
            systemMsg.setRole("system");
            systemMsg.setContent("Generated topic: " + result.getTopicDescription());
            contextManager.appendRound(sessionId, resolvedPrompt, rawResponse);

            long durationMs = System.currentTimeMillis() - startTime;
            log.info("Topic generation completed. sessionId={}, taskType=TOPIC, templateId={}, templateVersion={}, durationMs={}, success={}",
                    sessionId, templateId, version, durationMs, !result.isDegraded());

            return result;

        } catch (Exception e) {
            log.error("Topic generation failed. sessionId={}", sessionId, e);

            TopicGenerateResult degradedResult = new TopicGenerateResult();
            degradedResult.setDegraded(true);
            degradedResult.setTopicDescription("题目生成失败");
            degradedResult.setWritingRequirements("");
            degradedResult.setWordCount(0);
            degradedResult.setDifficulty("未知");

            long durationMs = System.currentTimeMillis() - startTime;
            log.info("Topic generation failed (degraded). sessionId={}, taskType=TOPIC, durationMs={}, success=false",
                    sessionId, durationMs);

            return degradedResult;
        }
    }

    public ReferenceResult generateReference(EssayReferenceRequest request) {
        String sessionId = request.getSessionId();
        String topic = request.getTopic();
        String essayTypeStr = request.getEssayType();

        long startTime = System.currentTimeMillis();

        try {
            String templateId = templateService.getLockedTemplate(sessionId, "REFERENCE", essayTypeStr);
            String version = "1.0";

            Map<String, Object> variables = new HashMap<>();
            variables.put("essay_type", essayTypeStr);
            variables.put("topic", topic);

            String resolvedPrompt = templateService.resolveTemplate(templateId, version, variables);

            List<MapMessage> messages = new ArrayList<>();
            MapMessage userMessage = new MapMessage();
            userMessage.setRole("user");
            userMessage.setContent(resolvedPrompt);
            messages.add(userMessage);

            String systemPrompt = "You are a reference essay generator.";
            String rawResponse = claudeClient.chat(systemPrompt, messages, DEFAULT_TEMPERATURE, DEFAULT_MAX_TOKENS);

            ReferenceResult result = responseParser.parseReferenceResult(rawResponse);

            contextManager.appendRound(sessionId, resolvedPrompt, rawResponse);

            long durationMs = System.currentTimeMillis() - startTime;
            log.info("Reference generation completed. sessionId={}, taskType=REFERENCE, templateId={}, templateVersion={}, durationMs={}, success={}",
                    sessionId, templateId, version, durationMs, !result.isDegraded());

            return result;

        } catch (Exception e) {
            log.error("Reference generation failed. sessionId={}", sessionId, e);

            ReferenceResult degradedResult = new ReferenceResult();
            degradedResult.setDegraded(true);
            degradedResult.setReferenceEssay("参考作文生成失败");
            degradedResult.setWordCount(0);
            degradedResult.setHighlights(new ArrayList<>());

            long durationMs = System.currentTimeMillis() - startTime;
            log.info("Reference generation failed (degraded). sessionId={}, taskType=REFERENCE, durationMs={}, success=false",
                    sessionId, durationMs);

            return degradedResult;
        }
    }
}