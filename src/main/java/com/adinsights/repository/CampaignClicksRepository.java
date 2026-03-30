package com.adinsights.repository;

import com.adinsights.model.CampaignClicks;
import com.adinsights.model.CampaignClicksKey;
import org.springframework.data.cassandra.repository.*;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface CampaignClicksRepository
        extends CassandraRepository<CampaignClicks, CampaignClicksKey> {

    @Query("""
        SELECT * FROM campaign_clicks
        WHERE tenant_id = ?0
        AND campaign_id = ?1
        AND event_date = ?2
        AND event_time >= ?3
        AND event_time <= ?4
    """)
    List<CampaignClicks> findClicksByTimeRange(
            String tenantId,
            String campaignId,
            LocalDate eventDate,
            Instant start,
            Instant end
    );
}