package com.example.adinsights.model;

import lombok.Data;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

@Table("campaign_clicks")
@Data
public class CampaignClicks {

    @PrimaryKeyColumn(name = "campaign_id", type = PrimaryKeyType.PARTITIONED)
    private String campaignId;

    @PrimaryKeyColumn(name = "tenant_id", type = PrimaryKeyType.CLUSTERED)
    private String tenantId;

    private int clicks;
}