package com.essay.agent.common.context;

import java.util.HashMap;
import java.util.Map;

public class SessionContext {

    private static final ThreadLocal<Map<String, Object>> context = ThreadLocal.withInitial(HashMap::new);

    public static void set(String key, Object value) {
        context.get().put(key, value);
    }

    public static Object get(String key) {
        return context.get().get(key);
    }

    public static String getSessionId() {
        return (String) get("sessionId");
    }

    public static void setSessionId(String sessionId) {
        set("sessionId", sessionId);
    }

    public static void clear() {
        context.get().clear();
    }
}