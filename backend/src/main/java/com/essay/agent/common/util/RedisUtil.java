package com.essay.agent.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class RedisUtil {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    public void set(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        Object value = get(key);
        return value != null ? (T) value : null;
    }

    public boolean delete(String key) {
        return Boolean.TRUE.equals(redisTemplate.delete(key));
    }

    public long delete(Collection<String> keys) {
        Long count = redisTemplate.delete(keys);
        return count != null ? count : 0;
    }

    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public boolean expire(String key, long timeout, TimeUnit unit) {
        return Boolean.TRUE.equals(redisTemplate.expire(key, timeout, unit));
    }

    public long getExpire(String key) {
        Long expire = redisTemplate.getExpire(key);
        return expire != null ? expire : -1;
    }

    public long increment(String key) {
        Long result = redisTemplate.opsForValue().increment(key);
        return result != null ? result : 0;
    }

    public long increment(String key, long delta) {
        Long result = redisTemplate.opsForValue().increment(key, delta);
        return result != null ? result : 0;
    }

    public long decrement(String key) {
        Long result = redisTemplate.opsForValue().decrement(key);
        return result != null ? result : 0;
    }

    public long decrement(String key, long delta) {
        Long result = redisTemplate.opsForValue().decrement(key, delta);
        return result != null ? result : 0;
    }

    public Long leftPush(String key, Object value) {
        return redisTemplate.opsForList().leftPush(key, value);
    }

    public Long rightPush(String key, Object value) {
        return redisTemplate.opsForList().rightPush(key, value);
    }

    public Object leftPop(String key) {
        return redisTemplate.opsForList().leftPop(key);
    }

    public Object rightPop(String key) {
        return redisTemplate.opsForList().rightPop(key);
    }

    public List<Object> range(String key, long start, long end) {
        return redisTemplate.opsForList().range(key, start, end);
    }

    public void trim(String key, long start, long end) {
        redisTemplate.opsForList().trim(key, start, end);
    }

    public Long size(String key) {
        return redisTemplate.opsForList().size(key);
    }

    public boolean remove(String key, long count, Object value) {
        return redisTemplate.opsForList().remove(key, count, value) > 0;
    }

    public boolean remove(String key, Object value) {
        return remove(key, 1, value);
    }

    public Long addSet(String key, Object... values) {
        return redisTemplate.opsForSet().add(key, values);
    }

    public Set<Object> members(String key) {
        return redisTemplate.opsForSet().members(key);
    }

    public boolean isMember(String key, Object value) {
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, value));
    }

    public long removeSet(String key, Object... values) {
        Long count = redisTemplate.opsForSet().remove(key, values);
        return count != null ? count : 0;
    }

    public long setCard(String key) {
        Long size = redisTemplate.opsForSet().size(key);
        return size != null ? size : 0;
    }

    public void putHash(String key, String hashKey, Object value) {
        redisTemplate.opsForHash().put(key, hashKey, value);
    }

    public Object getHash(String key, String hashKey) {
        return redisTemplate.opsForHash().get(key, hashKey);
    }

    @SuppressWarnings("unchecked")
    public <T> T getHash(String key, String hashKey, Class<T> clazz) {
        Object value = getHash(key, hashKey);
        return value != null ? (T) value : null;
    }

    public Map<Object, Object> entries(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    public boolean deleteHash(String key, Object... hashKeys) {
        return redisTemplate.opsForHash().delete(key, hashKeys) > 0;
    }

    public boolean hasHashKey(String key, String hashKey) {
        return redisTemplate.opsForHash().hasKey(key, hashKey);
    }

}