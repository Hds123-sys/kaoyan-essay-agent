package com.essay.agent.common.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WordCounter {

    private static final Pattern WORD_PATTERN = Pattern.compile("[a-zA-Z]+");

    private WordCounter() {
    }

    /**
     * 统计英文单词数
     * 按空格、标点、emoji分割，连续英文字母序列计为一个词
     */
    public static int count(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }

        Matcher matcher = WORD_PATTERN.matcher(text);
        int count = 0;
        while (matcher.find()) {
            count++;
        }

        return count;
    }

}