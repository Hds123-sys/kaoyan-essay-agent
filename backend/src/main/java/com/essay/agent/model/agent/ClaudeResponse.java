package com.essay.agent.model.agent;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClaudeResponse {

    private String id;

    private String type;

    private String role;

    private List<ContentBlock> content;

    @JsonProperty("stop_reason")
    private String stopReason;

    private Usage usage;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContentBlock {
        private String type;
        private String text;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Usage {
        @JsonProperty("input_tokens")
        private Integer inputTokens;

        @JsonProperty("output_tokens")
        private Integer outputTokens;
    }

}