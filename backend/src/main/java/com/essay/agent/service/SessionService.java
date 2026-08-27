package com.essay.agent.service;

import com.essay.agent.common.context.SessionContext;
import com.essay.agent.common.constant.RedisKeyConstants;
import com.essay.agent.common.util.RedisUtil;
import com.essay.agent.model.dto.response.SessionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
public class SessionService {

    @Autowired
    private RedisUtil redisUtil;

    public SessionResponse createSession() {
        String sessionId = UUID.randomUUID().toString().replace("-", "");

        // 保存会话信息
        String metaKey = RedisKeyConstants.buildSessionMetaKey(sessionId);
        redisUtil.set(metaKey, SessionResponse.builder()
                .sessionId(sessionId)
                .status("active")
                .createdAt(LocalDateTime.now())
                .lastActiveAt(LocalDateTime.now())
                .roundCount(0)
                .build());

        log.info("Session created - sessionId: {}", sessionId);

        return SessionResponse.builder()
                .sessionId(sessionId)
                .status("active")
                .createdAt(LocalDateTime.now())
                .lastActiveAt(LocalDateTime.now())
                .roundCount(0)
                .build();
    }

    public SessionResponse getSession(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            throw new RuntimeException("会话ID不能为空");
        }

        String metaKey = RedisKeyConstants.buildSessionMetaKey(sessionId);
        SessionResponse session = redisUtil.get(metaKey, SessionResponse.class);

        if (session == null) {
            throw new RuntimeException("会话不存在");
        }

        // 更新最后活跃时间
        session.setLastActiveAt(LocalDateTime.now());
        redisUtil.set(metaKey, session);

        return session;
    }

    public void clearSession(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            throw new RuntimeException("会话ID不能为空");
        }

        // 清空会话上下文
        contextManager.clearContext(sessionId);

        // 清空会话元数据
        String metaKey = RedisKeyConstants.buildSessionMetaKey(sessionId);
        redisUtil.delete(metaKey);

        log.info("Session cleared - sessionId: {}", sessionId);
    }

    @Autowired
    private ContextManager contextManager;
}