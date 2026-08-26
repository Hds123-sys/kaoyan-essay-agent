package com.essay.agent.common.util;

import java.util.Arrays;
import java.util.List;

public class SensitiveWordFilter {

    private static final List<String> SENSITIVE_WORDS = Arrays.asList(
            "killing", "violence", "death", "drugs", "illegal", "fraud",
            "gambling", "porn", "weapon", "bomb"
    );

    public static boolean containsSensitiveWord(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }

        String lowerText = text.toLowerCase();
        for (String word : SENSITIVE_WORDS) {
            if (lowerText.contains(word)) {
                return true;
            }
        }
        return false;
    }
}