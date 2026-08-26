package com.essay.agent.service;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ContextManager {

    public List<MapMessage> getTruncatedContext(String sessionId) {
        return new ArrayList<>();
    }

    public void appendRound(String sessionId, String userMsg, String assistantMsg) {
    }
}