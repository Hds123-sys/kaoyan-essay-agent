package com.essay.agent.model.agent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContextTruncateResult {

    private List<Message> messages;

    private boolean summaryDegraded;

    private String summary;

    private int originalRoundCount;

    private int finalRoundCount;

}