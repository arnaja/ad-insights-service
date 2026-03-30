package com.adinsights.service;

import com.adinsights.dto.MetricResponse;
import com.adinsights.exception.AdInsightsException;
import com.adinsights.exception.DependencyException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import static com.adinsights.utils.LogUtils.kv;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdInsightsService {

    private final RedisRealtimeService redis;
    private final CassandraHistoricalService cassandra;

    private static final Logger log =
            LoggerFactory.getLogger(AdInsightsService.class);

    @CircuitBreaker(name = "adService", fallbackMethod = "fallback")
    public MetricResponse getClicks(String tenantId, String campaignId,
                                    Instant start, Instant end) {

        try {

            CompletableFuture<Integer> realtime =
                    CompletableFuture.supplyAsync(() ->
                            redis.getClicks(tenantId, campaignId));

            CompletableFuture<Integer> historical =
                    CompletableFuture.supplyAsync(() ->
                            cassandra.getClicks(tenantId, campaignId, start, end));

            return MetricResponse.builder()
                    .metric("clicks")
                    .count(realtime.join() + historical.join())
                    .source("HYBRID")
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

    public MetricResponse fallback(String t, String c,
                                   Instant s, Instant e, Throwable ex) {
        return MetricResponse.builder()
                .metric("clicks")
                .count(0)
                .source("FALLBACK")
                .timestamp(Instant.now())
                .build();
    }
}