package com.adinsights.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ad-insights.routing")
public class RoutingProperties {

    private long realtimeWindowMinutes;
    private long snowflakeWindowDays;
}