package com.adinsights.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignMetricRecord {

    private String pk;          // TENANT#{tenantId}#CAMPAIGN#{campaignId}
    private String sk;          // DATE#{yyyy-MM-dd}#TS#{epochMillis}
    private String tenantId;
    private String campaignId;
    private String metricType;  // clicks / impressions / clickToBasket
    private Instant eventTime;
    private Integer count;
}