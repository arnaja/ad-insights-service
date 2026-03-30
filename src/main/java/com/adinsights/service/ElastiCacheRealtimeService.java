package com.adinsights.service;

import com.adinsights.exception.DependencyException;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;

import static com.adinsights.util.LogUtils.kv;

@Slf4j
@Service
@RequiredArgsConstructor
public class ElastiCacheRealtimeService {

    private final RedisTemplate<String, Integer> redisTemplate;

    @Retry(name = "redisRetry")
    @CircuitBreaker(name = "redisCB", fallbackMethod = "fallbackGetClicks")
    @Bulkhead(name = "redisBulkhead")
    public int getClicks(String tenantId, String campaignId) {
        try {
            String key = buildKey(tenantId, campaignId, "clicks");
            Integer value = redisTemplate.opsForValue().get(key);

            log.info("ElastiCache read",
                    kv("tenantId", tenantId),
                    kv("campaignId", campaignId),
                    kv("cacheKey", key));

            return value == null ? 0 : value;
        } catch (Exception ex) {
            throw new DependencyException("ElastiCache read failed", ex);
        }
    }

    public int fallbackGetClicks(String tenantId, String campaignId, Instant start, Instant end, Throwable throwable) {
        log.warn("ElastiCache fallback",
                kv("tenantId", tenantId),
                kv("campaignId", campaignId),
                kv("reason", throwable.getClass().getSimpleName()));
        return 0;
    }

    private String buildKey(String tenantId, String campaignId, String metricType) {
        return "tenant:" + tenantId + ":campaign:" + campaignId + ":metric:" + metricType;
    }
}