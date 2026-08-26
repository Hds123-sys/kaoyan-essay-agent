package com.essay.agent.common.context;

public class SessionContext {

    private static final ThreadLocal<String> SESSION_ID = new ThreadLocal<>();

    private SessionContext() {
    }

    public static void set(String sessionId) {
        SESSION_ID.set(sessionId);
    }

    public static String get() {
        return SESSION_ID.get();
    }

    public static void clear() {
        SESSION_ID.remove();
    }

}