package com.essay.agent.common.util;

import com.essay.agent.model.agent.Message;

import java.util.List;

public class TokenEstimator {

    private static final int ENGLISH_CHARS_PER_TOKEN = 4;
    private static final double CHINESE_CHARS_PER_TOKEN = 1.7;

    private TokenEstimator() {
    }

    /**
     * 估算文本的token数（粗估）
     * 英文：1 token ≈ 4个字符
     * 中文：1 token ≈ 1.7个汉字
     * 注意：Claude API实际使用BPE分词，此估算存在误差，
     * 实际消耗以 Claude API 返回的 usage 字段为准
     */
    public static int estimate(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }

        int englishChars = 0;
        int chineseChars = 0;

        for (char c : text.toCharArray()) {
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
                englishChars++;
            } else if (c >= '\u4e00' && c <= '\u9fa5') {
                chineseChars++;
            }
        }

        int englishTokens = englishChars / ENGLISH_CHARS_PER_TOKEN;
        int chineseTokens = (int) Math.ceil(chineseChars / CHINESE_CHARS_PER_TOKEN);

        return englishTokens + chineseTokens;
    }

    /**
     * 估算消息列表的总token数
     */
    public static int estimateMessages(List<Message> messages) {
        if (messages == null || messages.isEmpty()) {
            return 0;
        }

        int total = 0;
        for (Message message : messages) {
            total += estimate(message.getContent());
            total += estimate(message.getRole());
        }

        return total;
    }

}