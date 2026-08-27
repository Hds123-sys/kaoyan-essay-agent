package com.essay.agent.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class RedisLockUtil {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String RELEASE_LUA_SCRIPT =
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
            "    return redis.call('del', KEYS[1]) " +
            "else " +
            "    return 0 " +
            "end";

    public String generateLockValue() {
        return UUID.randomUUID().toString() + Thread.currentThread().getId();
    }

    public boolean tryLock(String key, String value, long timeoutSeconds) {
        Boolean result = redisTemplate.opsForValue().setIfAbsent(key, value, timeoutSeconds, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(result);
    }

    public boolean releaseLock(String key, String value) {
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(RELEASE_LUA_SCRIPT, Long.class);
        Long result = redisTemplate.execute(redisScript, Collections.singletonList(key), value);
        return result != null && result == 1L;
    }
}