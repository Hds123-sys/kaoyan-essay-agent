package com.essay.agent.model;

import lombok.Data;

@Data
public class ErrorItem {
    private String original;
    private String corrected;
    private String reason;
    private String type;
}