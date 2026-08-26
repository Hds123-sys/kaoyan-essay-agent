package com.essay.agent.common.constant;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RedisKeyConstants {

    private static final DateTimeFormatter MINUTE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

    private RedisKeyConstants() {
    }

    public static String buildSessionKey(String sessionId) {
        return String.format("session:%s", sessionId);
    }

    public static String buildLockKey(String sessionId) {
        return String.format("lock:session:%s", sessionId);
    }

    public static String buildRateLimitKey(String sessionId) {
        return String.format("ratelimit:%s", sessionId);
    }

    public static String buildTemplateLockKey(String sessionId) {
        return String.format("template:lock:%s", sessionId);
    }

    public static String buildTemplateCacheKey(String templateId, String version) {
        return String.format("template:cache:%s:%s", templateId, version);
    }

    public static String buildVocabKey(String sessionId) {
        return String.format("vocab:%s", sessionId);
    }

    public static String buildTpmKey() {
        return String.format("tpm:global:%s", LocalDateTime.now().format(MINUTE_FORMATTER));
    }

    public static String buildTpmKey(LocalDateTime time) {
        return String.format("tpm:global:%s", time.format(MINUTE_FORMATTER));
    }

    public static String buildSessionMetaKey(String sessionId) {
        return String.format("session:%s:meta", sessionId);
    }

}