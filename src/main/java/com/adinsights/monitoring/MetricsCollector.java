package com.adinsights.monitoring;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class MetricsCollector {

    private final MeterRegistry registry;

    public void recordLatency(long millis) {
        registry.timer("ad.metrics.latency").record(millis, TimeUnit.MILLISECONDS);
    }

    public void incrementRequests() {
        registry.counter("ad.metrics.requests").increment();
    }
}