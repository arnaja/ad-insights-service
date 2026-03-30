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

import static com.adinsights.utils.LogUtils.kv;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdInsightsService {

    private final RedisRealtimeService redisService;
    private final CassandraHistoricalService cassandraService;
    private final RoutingProperties routingProperties;

    private static final Logger log =
            LoggerFactory.getLogger(AdInsightsService.class);

    @CircuitBreaker(name = "adService", fallbackMethod = "fallback")
    public MetricResponse getClicks(String tenantId, String campaignId,
                                    Instant start, Instant end) {

        long startTime = System.currentTimeMillis();

        try {
            validate(start, end);

            Duration realtimeWindow =
                    Duration.ofMinutes(routingProperties.getRealtimeWindowMinutes());

            Instant threshold = Instant.now().minus(realtimeWindow);

            log.info("Routing request",
                    kv("tenantId", tenantId),
                    kv("campaignId", campaignId),
                    kv("realtimeWindowMinutes",
                            routingProperties.getRealtimeWindowMinutes()));

            int result;
            String source = null;

            // ✅ Fully real-time
            if (start.isAfter(threshold)) {

                source = "REAL-TIME";
                result = redisService.getClicks(tenantId, campaignId);

                log.info("Served from Redis",
                        kv("latencyMs", latency(startTime)));

            }
            // ✅ Fully historical
            else if (end.isBefore(threshold)) {

                source = "HISTORICAL";
                result = cassandraService.getClicks(tenantId, campaignId, start, end);

                log.info("Served from Cassandra",
                        kv("latencyMs", latency(startTime)));
            }
            // ✅ Hybrid
            else {

                source = "HYBRID";
                CompletableFuture<Integer> realtimePart =
                        CompletableFuture.supplyAsync(() ->
                                redisService.getClicks(tenantId, campaignId));

                CompletableFuture<Integer> historicalPart =
                        CompletableFuture.supplyAsync(() ->
                                cassandraService.getClicks(tenantId, campaignId, start, end));

                log.info("Hybrid routing",
                        kv("historicalPart", historicalPart.get()),
                        kv("realtimePart", realtimePart.get()),
                        kv("latencyMs", latency(startTime)));
                result = historicalPart.get() + realtimePart.get();
                return MetricResponse.builder()
                        .metric("clicks")
                        .count(realtimePart.join() + historicalPart.join())
                        .source("HYBRID")
                        .timestamp(Instant.now())
                        .build();
            }
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

    private long latency(long startTime) {
        return System.currentTimeMillis() - startTime;
    }
}