package com.adinsights.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;

@Repository
@RequiredArgsConstructor
public class SnowflakeMetricsRepository {

    private final JdbcTemplate jdbcTemplate;

    public int getMetricCount(String tenantId,
                              String campaignId,
                              String metricType,
                              Instant start,
                              Instant end) {

        String sql = """
                SELECT COALESCE(SUM(metric_count), 0)
                FROM campaign_metrics_agg
                WHERE tenant_id = ?
                  AND campaign_id = ?
                  AND metric_type = ?
                  AND event_time >= ?
                  AND event_time <= ?
                """;

        Integer result = jdbcTemplate.queryForObject(
                sql,
                Integer.class,
                tenantId,
                campaignId,
                metricType,
                Timestamp.from(start),
                Timestamp.from(end)
        );

        return result == null ? 0 : result;
    }
}