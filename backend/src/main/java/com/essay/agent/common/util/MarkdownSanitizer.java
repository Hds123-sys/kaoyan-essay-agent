package com.essay.agent.common.util;

public class MarkdownSanitizer {

    private MarkdownSanitizer() {
    }

    /**
     * Markdown XSS净化
     * 过滤危险协议和标签
     */
    public static String sanitize(String markdown) {
        if (markdown == null || markdown.isEmpty()) {
            return markdown;
        }

        String result = markdown;

        result = sanitizeAHref(result);
        result = sanitizeImgSrc(result);
        result = removeDangerousTags(result);
        result = removeEventHandlers(result);

        return result;
    }

    private static String sanitizeAHref(String markdown) {
        return markdown.replaceAll(
                "(\\[.*?\\]\\(\\s*)(javascript:|data:|vbscript:)",
                "$1#"
        );
    }

    private static String sanitizeImgSrc(String markdown) {
        return markdown.replaceAll(
                "(\\!\\[.*?\\]\\(\\s*)(javascript:|data:)",
                "$1"
        );
    }

    private static String removeDangerousTags(String markdown) {
        String result = markdown;

        result = result.replaceAll("(?i)<script[^>]*>.*?</script>", "");
        result = result.replaceAll("(?i)<iframe[^>]*>.*?</iframe>", "");
        result = result.replaceAll("(?i)<object[^>]*>.*?</object>", "");
        result = result.replaceAll("(?i)<embed[^>]*>", "");
        result = result.replaceAll("(?i)<form[^>]*>.*?</form>", "");

        return result;
    }

    private static String removeEventHandlers(String markdown) {
        return markdown.replaceAll("\\s+on\\w+\\s*=\\s*\"[^\"]*\"", "")
                .replaceAll("\\s+on\\w+\\s*=\\s*'[^']*'", "")
                .replaceAll("\\s+on\\w+\\s*=\\s*[^\\s>]+", "");
    }

}