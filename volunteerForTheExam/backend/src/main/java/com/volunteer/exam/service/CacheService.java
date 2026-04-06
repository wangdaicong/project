package com.volunteer.exam.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Redis缓存服务（支持优雅降级）
 */
@Service
public class CacheService {

    private static final Logger log = LoggerFactory.getLogger(CacheService.class);

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 设置缓存
     */
    public void set(String key, Object value) {
        try {
            if (redisTemplate != null) {
                redisTemplate.opsForValue().set(key, value);
            }
        } catch (Exception e) {
            log.warn("Redis缓存设置失败，key: {}, 错误: {}", key, e.getMessage());
        }
    }

    /**
     * 设置缓存并设置过期时间
     */
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        try {
            if (redisTemplate != null) {
                redisTemplate.opsForValue().set(key, value, timeout, unit);
            }
        } catch (Exception e) {
            log.warn("Redis缓存设置失败，key: {}, 错误: {}", key, e.getMessage());
        }
    }

    /**
     * 获取缓存
     */
    public Object get(String key) {
        try {
            if (redisTemplate != null) {
                return redisTemplate.opsForValue().get(key);
            }
        } catch (Exception e) {
            log.warn("Redis缓存获取失败，key: {}, 错误: {}", key, e.getMessage());
        }
        return null;
    }

    /**
     * 删除缓存
     */
    public Boolean delete(String key) {
        try {
            if (redisTemplate != null) {
                return redisTemplate.delete(key);
            }
        } catch (Exception e) {
            log.warn("Redis缓存删除失败，key: {}, 错误: {}", key, e.getMessage());
        }
        return false;
    }

    /**
     * 判断缓存是否存在
     */
    public Boolean hasKey(String key) {
        try {
            if (redisTemplate != null) {
                return redisTemplate.hasKey(key);
            }
        } catch (Exception e) {
            log.warn("Redis缓存检查失败，key: {}, 错误: {}", key, e.getMessage());
        }
        return false;
    }

    /**
     * 设置过期时间
     */
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        try {
            if (redisTemplate != null) {
                return redisTemplate.expire(key, timeout, unit);
            }
        } catch (Exception e) {
            log.warn("Redis缓存过期时间设置失败，key: {}, 错误: {}", key, e.getMessage());
        }
        return false;
    }
}
