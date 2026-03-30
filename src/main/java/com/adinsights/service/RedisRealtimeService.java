package com.adinsights.service;

import com.adinsights.exception.DependencyException;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;

import static com.adinsights.utils.LogUtils.kv;

@Service
@RequiredArgsConstructor
@Retry(name = "redisRetry")
@CircuitBreaker(name = "redisCB", fallbackMethod = "fallback")
@Bulkhead(name = "redisBulkhead", type = Bulkhead.Type.THREADPOOL)
public class RedisRealtimeService {

    private final RedisTemplate<String, Integer> redisTemplate;
    private static final Logger log =
            LoggerFactory.getLogger(RedisRealtimeService.class);

    public int getClicks(String tenantId, String campaignId) {
        try {
            String key = "clicks:" + tenantId + ":" + campaignId;
            Integer val = redisTemplate.opsForValue().get(key);

            log.debug("Fetching from Redis",
                    kv("key", key),
                    kv("tenantId", tenantId));
            return val != null ? val : 0;
        } catch (Exception ex) {
            log.error("Error while fetching from Redis",
                    kv("tenantId", tenantId),
                    kv("campaignId", campaignId),
                    ex);
        throw new DependencyException("Redis failure", ex);
        }
    }
    public int fallback(String tenantId, String campaignId,
                        Instant start, Instant end, Throwable ex) {

        log.error("Redis fallback triggered", ex);
        return 0; // graceful degradation
    }
}