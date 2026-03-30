package com.adinsights.service;

import com.adinsights.config.RoutingProperties;
import com.adinsights.dto.MetricResponse;
import com.adinsights.exception.AdInsightsException;
import com.adinsights.exception.DependencyException;
import com.adinsights.exception.ValidationException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import static com.adinsights.util.LogUtils.kv;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdInsightsService {

    private final ElastiCacheRealtimeService elastiCacheRealtimeService;
    private final DynamoDbHistoricalService dynamoDbHistoricalService;
    private final SnowflakeAnalyticsService snowflakeAnalyticsService;
    private final RoutingProperties routingProperties;

    private static final Logger log =
            LoggerFactory.getLogger(AdInsightsService.class);

    @CircuitBreaker(name = "adService", fallbackMethod = "fallback")
    public MetricResponse getClicks(String tenantId, String campaignId,
                                    Instant start, Instant end) {

        long requestStart = System.currentTimeMillis();

        try {
            validate(start, end);

            Duration realtimeWindow =
                    Duration.ofMinutes(routingProperties.getRealtimeWindowMinutes());

            Duration analyticsWindow = Duration.ofDays(
                    routingProperties.getSnowflakeWindowDays()
            );
            log.info("Routing request",
                    kv("tenantId", tenantId),
                    kv("campaignId", campaignId),
                    kv("realtimeWindow",realtimeWindow),
                    kv("analyticsWindow",analyticsWindow));

            Instant now = Instant.now();
            Instant realtimeThreshold = now.minus(realtimeWindow);
            Instant analyticsThreshold = now.minus(analyticsWindow);

            int result;
            String source;

            // recent -> ElastiCache
            if (start.isAfter(realtimeThreshold)) {
                result = elastiCacheRealtimeService.getClicks(tenantId, campaignId);
                source = "ELASTICACHE";
            }
            // very old / long-range -> Snowflake
            else if (end.isBefore(analyticsThreshold)) {
                result = snowflakeAnalyticsService.getClicks(tenantId, campaignId, start, end);
                source = "SNOWFLAKE";
            }
            // medium-range -> DynamoDB
            else if (end.isBefore(realtimeThreshold)) {
                result = dynamoDbHistoricalService.getClicks(tenantId, campaignId, start, end);
                source = "DYNAMODB";
            }
            // overlap recent + historical
            else {
                source = "HYBRID_ELASTICACHE_DYNAMODB";
                CompletableFuture<Integer> realtimePart =
                        CompletableFuture.supplyAsync(() ->
                                elastiCacheRealtimeService.getClicks(tenantId, campaignId));

                CompletableFuture<Integer> historicalPart =
                        CompletableFuture.supplyAsync(() ->
                                dynamoDbHistoricalService.getClicks(tenantId, campaignId, start, end));
                result = historicalPart.get() + realtimePart.get();
            }
            log.info("Completed clicks query",
                    kv("tenantId", tenantId),
                    kv("campaignId", campaignId),
                    kv("source", source),
                    kv("latencyMs", System.currentTimeMillis() - requestStart),
                    kv("count", result));

            return MetricResponse.builder()
                    .metric("clicks")
                    .count(result)
                    .source(source)
                    .timestamp(Instant.now())
                    .build();
        } catch (DependencyException ex) {
            log.error("Service failure",
                    kv("tenantId", tenantId),
                    kv("campaignId", campaignId),
                    ex);
            // fallback strategy
            throw ex;

        } catch (Exception ex) {
            log.error("Service failure",
                    kv("tenantId", tenantId),
                    kv("campaignId", campaignId),
                    ex);
            throw new AdInsightsException("Service failure", "SERVICE_ERROR");
        }
    }

    public MetricResponse fallback(String tenantId, String campaignId,
                                   Instant start, Instant end, Throwable ex) {
        return MetricResponse.builder()
                .metric("clicks")
                .count(0)
                .source("FALLBACK")
                .timestamp(Instant.now())
                .build();
    }
    // =========================
    // Validation
    // =========================

    private void validate(Instant start, Instant end) {
        if (start == null || end == null) {
            throw new ValidationException("Start and End cannot be null");
        }
        if (start.isAfter(end)) {
            throw new ValidationException("Start cannot be after End");
        }
    }
}