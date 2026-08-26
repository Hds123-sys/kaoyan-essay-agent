package com.essay.agent.common.util;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class LanguageDetector {

    private static final Pattern ENGLISH_PATTERN = Pattern.compile("[a-zA-Z]");
    private static final double ENGLISH_THRESHOLD = 0.8;

    public static boolean isEnglishEssay(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }

        String cleanedText = text.replaceAll("\\s+", "");
        if (cleanedText.isEmpty()) {
            return false;
        }

        int totalChars = cleanedText.length();
        int englishChars = 0;

        for (char c : cleanedText.toCharArray()) {
            if (ENGLISH_PATTERN.matcher(String.valueOf(c)).matches()) {
                englishChars++;
            }
        }

        double englishRatio = (double) englishChars / totalChars;
        return englishRatio >= ENGLISH_THRESHOLD;
    }
}