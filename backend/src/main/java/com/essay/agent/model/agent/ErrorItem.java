package com.essay.agent.model.agent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorItem {

    private String original;

    private String corrected;

    private String reason;

    private String type;

}