package com.essay.agent.common.util;

public class LanguageDetector {

    private static final double ENGLISH_RATIO_THRESHOLD = 0.8;

    private LanguageDetector() {
    }

    /**
     * 检测英语字符占比
     * 分母 = 英文字符数(a-z,A-Z) + 非英文字母字符数(中文/日文等Unicode字母)
     * 标点、数字、空格、emoji不计入分子和分母
     * @return 英语占比 0.0 ~ 1.0
     */
    public static double englishRatio(String text) {
        if (text == null || text.isEmpty()) {
            return 0.0;
        }

        int englishChars = 0;
        int otherLetterChars = 0;

        for (char c : text.toCharArray()) {
            if (isEnglishLetter(c)) {
                englishChars++;
            } else if (isLetter(c) && !isEnglishLetter(c)) {
                otherLetterChars++;
            }
        }

        int totalLetterChars = englishChars + otherLetterChars;
        if (totalLetterChars == 0) {
            return 0.0;
        }

        return (double) englishChars / totalLetterChars;
    }

    /**
     * 是否满足英语占比>=80%
     */
    public static boolean isEnglishEssay(String text) {
        return englishRatio(text) >= ENGLISH_RATIO_THRESHOLD;
    }

    private static boolean isEnglishLetter(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    private static boolean isLetter(char c) {
        return Character.isLetter(c);
    }

}