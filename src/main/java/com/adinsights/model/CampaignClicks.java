package com.adinsights.model;

import lombok.*;
import org.springframework.data.cassandra.core.mapping.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("campaign_clicks")
public class CampaignClicks {

    /**
     * Composite Primary Key:
     * (tenant_id, campaign_id, event_date) -> partition keys
     * event_time -> clustering column
     */
    @PrimaryKey
    private CampaignClicksKey key;

    /**
     * Number of clicks recorded for this event row.
     * In high-scale systems, this may represent aggregated counts
     * (e.g., per minute bucket) instead of raw events.
     */
    @Column("clicks")
    private int clicks;

    /**
     * Optional metadata for debugging / observability
     */
    @Column("source")
    private String source; // e.g., "web", "mobile", "partner"

    @Column("created_at")
    private java.time.Instant createdAt;
}