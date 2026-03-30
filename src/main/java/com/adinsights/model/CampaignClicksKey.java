package com.adinsights.model;

import lombok.*;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.*;

import java.time.Instant;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@PrimaryKeyClass
public class CampaignClicksKey {

    @PrimaryKeyColumn(name = "tenant_id", type = PrimaryKeyType.PARTITIONED)
    private String tenantId;

    @PrimaryKeyColumn(name = "campaign_id", type = PrimaryKeyType.PARTITIONED)
    private String campaignId;

    @PrimaryKeyColumn(name = "event_date", type = PrimaryKeyType.PARTITIONED)
    private LocalDate eventDate;

    @PrimaryKeyColumn(name = "event_time", ordinal = 0, type = PrimaryKeyType.CLUSTERED)
    private Instant eventTime;
}