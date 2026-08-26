package com.essay.agent.model.agent;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaudeRequest {

    private String model;

    @JsonProperty("max_tokens")
    private Integer maxTokens;

    private Double temperature;

    private List<Message> messages;

    private String system;

}