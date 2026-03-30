package com.adinsights.service;

import com.adinsights.exception.AdInsightsException;
import com.adinsights.exception.DependencyException;
import com.adinsights.model.CampaignClicks;
import com.adinsights.repository.CampaignClicksRepository;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static com.adinsights.utils.LogUtils.kv;

@Service
@RequiredArgsConstructor
@Retry(name = "cassandraRetry")
@CircuitBreaker(name = "cassandraCB")
@Bulkhead(name = "cassandraBulkhead", type = Bulkhead.Type.THREADPOOL)
public class CassandraHistoricalService {

    private final CampaignClicksRepository repository;
    private static final Logger log =
            LoggerFactory.getLogger(CassandraHistoricalService.class);

    public int getClicks(String tenantId, String campaignId,
                         Instant start, Instant end) {

        try {
            int total = 0;

            LocalDate current = start.atZone(ZoneId.of("UTC")).toLocalDate();
            LocalDate endDate = end.atZone(ZoneId.of("UTC")).toLocalDate();

            while (!current.isAfter(endDate)) {

                try {
                    log.info("Querying Cassandra",
                            kv("date", current),
                            kv("tenantId", tenantId),
                            kv("campaignId", campaignId));
                    List<CampaignClicks> rows =
                            repository.findClicksByTimeRange(
                                    tenantId, campaignId, current, start, end
                            );

                    total += rows.stream()
                            .mapToInt(CampaignClicks::getClicks)
                            .sum();

                } catch (Exception ex) {
                    log.error("Error while fetching from Cassandra",
                            kv("date", current),
                            kv("tenantId", tenantId),
                            kv("campaignId", campaignId),
                            ex);
                    throw new DependencyException(
                            "Cassandra query failed for date: " + current, ex);
                }

                current = current.plusDays(1);
            }

            return total;

        } catch (DependencyException ex) {
            log.error("Error while fetching from Cassandra",
                    kv("tenantId", tenantId),
                    kv("campaignId", campaignId),
                    ex);
            throw ex;
        } catch (Exception ex) {
            log.error("Error while fetching from Cassandra",
                    kv("tenantId", tenantId),
                    kv("campaignId", campaignId),
                    ex);
            throw new AdInsightsException("Historical fetch failed", "DB_ERROR");
        }
    }
}