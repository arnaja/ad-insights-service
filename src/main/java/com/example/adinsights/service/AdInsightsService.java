package com.example.adinsights.service;

import com.example.adinsights.dto.ClicksResponse;
import com.example.adinsights.repository.ClicksRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdInsightsService {

    private final ClicksRepository clicksRepository;
    private final RedisTemplate<String, Integer> redisTemplate;

    private static final String CACHE_KEY = "ad:clicks:";

    @Retry(name = "clickServiceRetry", fallbackMethod = "fallbackClicks")
    @CircuitBreaker(name = "clickServiceCB", fallbackMethod = "fallbackClicks")
    public ClicksResponse getClicks(String campaignId, String tenantId) {

        String key = CACHE_KEY + tenantId + ":" + campaignId;

        // 1. Check Redis Cache
        Integer cachedClicks = redisTemplate.opsForValue().get(key);
        if (cachedClicks != null) {
            log.info("Cache hit for campaign {}", campaignId);
            return buildResponse(campaignId, cachedClicks);
        }

        // 2. Fallback to DB (Cassandra/DynamoDB)
        log.info("Cache miss, fetching from DB for campaign {}", campaignId);
        int clicks = clicksRepository.getClicks(campaignId, tenantId);

        // 3. Update Cache
        redisTemplate.opsForValue().set(key, clicks, Duration.ofMinutes(5));

        return buildResponse(campaignId, clicks);
    }

    public ClicksResponse fallbackClicks(String campaignId, String tenantId, Throwable ex) {
        log.error("Fallback triggered for campaign {} and tenant {}", campaignId,tenantId, ex);
        return buildResponse(campaignId, 0);
    }

    private ClicksResponse buildResponse(String campaignId, int clicks) {
        return ClicksResponse.builder()
                .campaignId(campaignId)
                .clicks(clicks)
                .timestamp(Instant.now())
                .build();
    }
}