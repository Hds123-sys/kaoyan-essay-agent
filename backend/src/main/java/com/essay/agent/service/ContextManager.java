package com.essay.agent.service;

import com.essay.agent.common.constant.ErrorCodeConstants;
import com.essay.agent.common.constant.RedisKeyConstants;
import com.essay.agent.common.exception.BusinessException;
import com.essay.agent.common.util.RedisUtil;
import com.essay.agent.model.agent.ContextTruncateResult;
import com.essay.agent.model.agent.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ContextManager {

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private ClaudeClient claudeClient;

    @Autowired
    @Qualifier("summaryThreadPool")
    private ThreadPoolTaskExecutor summaryThreadPool;

    @Value("${agent.context.max-rounds:10}")
    private int maxRounds;

    @Value("${agent.context.keep-recent-rounds:2}")
    private int keepRecentRounds;

    @Value("${agent.context.trigger-threshold:4}")
    private int triggerThreshold;

    @Value("${agent.context.summary-timeout-seconds:15}")
    private int summaryTimeoutSeconds;

    private static final String SUMMARY_PROMPT = "你是一个对话摘要助手。请将以下考研英语作文批改对话历史压缩为一段不超过200字的摘要，保留关键信息：作文题目、作文类型、用户主要修改点、Agent指出的核心问题。不要保留细节。";
    private static final int SESSION_TTL_DAYS = 7;

    public ContextTruncateResult getTruncatedContext(String sessionId) {
        String sessionKey = RedisKeyConstants.buildSessionKey(sessionId);
        List<Object> messageObjects = redisUtil.range(sessionKey, 0, -1);

        List<Message> messages = messageObjects.stream()
                .map(obj -> (Message) obj)
                .collect(Collectors.toList());

        int roundCount = getRoundCountInternal(messages);
        log.debug("Session context - sessionId: {}, roundCount: {}", sessionId, roundCount);

        if (roundCount <= triggerThreshold) {
            return ContextTruncateResult.builder()
                    .messages(messages)
                    .summaryDegraded(false)
                    .originalRoundCount(roundCount)
                    .finalRoundCount(roundCount)
                    .build();
        }

        List<Message> oldMessages = new ArrayList<>();
        List<Message> recentMessages = new ArrayList<>();

        int splitIndex = (roundCount - keepRecentRounds) * 2;
        for (int i = 0; i < messages.size(); i++) {
            if (i < splitIndex) {
                oldMessages.add(messages.get(i));
            } else {
                recentMessages.add(messages.get(i));
            }
        }

        String summary = generateSummary(sessionId, oldMessages);

        List<Message> resultMessages = new ArrayList<>();
        if (summary != null) {
            Message summaryMessage = Message.builder()
                    .role("system")
                    .content("历史对话摘要：" + summary)
                    .build();
            resultMessages.add(summaryMessage);
            resultMessages.addAll(recentMessages);
            log.info("Summary generated successfully - sessionId: {}, summary: {}", sessionId, summary);
        } else {
            int startIndex = Math.min(4, oldMessages.size());
            resultMessages.addAll(oldMessages.subList(startIndex, oldMessages.size()));
            resultMessages.addAll(recentMessages);
            log.warn("Summary generation failed, using fallback - sessionId: {}", sessionId);
        }

        int finalRoundCount = resultMessages.size() / 2;
        return ContextTruncateResult.builder()
                .messages(resultMessages)
                .summaryDegraded(summary == null)
                .summary(summary)
                .originalRoundCount(roundCount)
                .finalRoundCount(finalRoundCount)
                .build();
    }

    private String generateSummary(String sessionId, List<Message> oldMessages) {
        try {
            CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                try {
                    List<Message> summaryMessages = new ArrayList<>(oldMessages);
                    summaryMessages.add(0, Message.builder().role("user").content(SUMMARY_PROMPT).build());

                    return claudeClient.extractText(claudeClient.chat(
                            "",
                            summaryMessages,
                            0.1,
                            500
                    ));
                } catch (Exception e) {
                    log.error("Summary generation failed - sessionId: {}", sessionId, e);
                    return null;
                }
            }, summaryThreadPool);

            return future.orTimeout(summaryTimeoutSeconds, TimeUnit.SECONDS)
                    .exceptionally(e -> {
                        if (e instanceof TimeoutException) {
                            log.warn("Summary generation timeout - sessionId: {}", sessionId);
                        }
                        return null;
                    })
                    .join();
        } catch (Exception e) {
            log.error("Summary generation error - sessionId: {}", sessionId, e);
            return null;
        }
    }

    public void appendRound(String sessionId, Message userMessage, Message assistantMessage) {
        int currentRounds = getRoundCount(sessionId);
        if (currentRounds >= maxRounds) {
            throw new BusinessException(ErrorCodeConstants.ESSAY_TYPE_REQUIRED,
                    "已超出本轮对话最大迭代次数，请清空上下文重新开始");
        }

        String sessionKey = RedisKeyConstants.buildSessionKey(sessionId);
        redisUtil.rightPush(sessionKey, userMessage);
        redisUtil.rightPush(sessionKey, assistantMessage);
        redisUtil.expire(sessionKey, SESSION_TTL_DAYS, TimeUnit.DAYS);

        log.debug("Round appended - sessionId: {}, roundCount: {}", sessionId, currentRounds + 1);
    }

    public void clearContext(String sessionId) {
        String sessionKey = RedisKeyConstants.buildSessionKey(sessionId);
        String templateLockKey = RedisKeyConstants.buildTemplateLockKey(sessionId);
        String vocabKey = RedisKeyConstants.buildVocabKey(sessionId);
        String sessionMetaKey = RedisKeyConstants.buildSessionMetaKey(sessionId);

        redisUtil.delete(sessionKey);
        redisUtil.delete(templateLockKey);
        redisUtil.delete(vocabKey);
        redisUtil.delete(sessionMetaKey);

        log.info("Context cleared - sessionId: {}", sessionId);
    }

    public void addMessage(String sessionId, Message message) {
        String sessionKey = RedisKeyConstants.buildSessionKey(sessionId);
        redisUtil.rightPush(sessionKey, message);
        redisUtil.expire(sessionKey, SESSION_TTL_DAYS, TimeUnit.DAYS);
    }

    public List<Message> getMessages(String sessionId, int maxTokens) {
        String sessionKey = RedisKeyConstants.buildSessionKey(sessionId);
        List<Object> messageObjects = redisUtil.range(sessionKey, 0, -1);

        if (messageObjects == null || messageObjects.isEmpty()) {
            return new ArrayList<>();
        }

        return messageObjects.stream()
                .map(obj -> (Message) obj)
                .collect(Collectors.toList());
    }

    public int getRoundCount(String sessionId) {
        String sessionKey = RedisKeyConstants.buildSessionKey(sessionId);
        List<Object> messages = redisUtil.range(sessionKey, 0, -1);
        return messages != null ? messages.size() / 2 : 0;
    }

    public String getCurrentTopic(String sessionId) {
        return null;
    }

    private int getRoundCountInternal(List<Message> messages) {
        return messages != null ? messages.size() / 2 : 0;
    }

}