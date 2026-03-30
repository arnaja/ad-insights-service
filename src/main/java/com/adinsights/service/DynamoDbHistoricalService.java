package com.adinsights.service;

import com.adinsights.exception.DependencyException;
import com.adinsights.repository.DynamoDbMetricsRepository;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

import static com.adinsights.util.LogUtils.kv;

@Slf4j
@Service
@RequiredArgsConstructor
public class DynamoDbHistoricalService {

    private final DynamoDbMetricsRepository repository;

    @Retry(name = "dynamoRetry")
    @CircuitBreaker(name = "dynamoCB", fallbackMethod = "fallbackGetClicks")
    @Bulkhead(name = "dynamoBulkhead")
    public int getClicks(String tenantId, String campaignId, Instant start, Instant end) {
        try {
            int total = repository.getMetricCount(tenantId, campaignId, "clicks", start, end);

            log.info("DynamoDB read",
                    kv("tenantId", tenantId),
                    kv("campaignId", campaignId),
                    kv("start", start),
                    kv("end", end),
                    kv("count", total));

            return total;
        } catch (Exception ex) {
            throw new DependencyException("DynamoDB read failed", ex);
        }
    }

    public int fallbackGetClicks(String tenantId, String campaignId, Instant start, Instant end, Throwable throwable) {
        log.warn("DynamoDB fallback",
                kv("tenantId", tenantId),
                kv("campaignId", campaignId),
                kv("reason", throwable.getClass().getSimpleName()));
        return 0;
    }
}