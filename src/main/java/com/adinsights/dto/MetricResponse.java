package com.adinsights.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class MetricResponse {
    private String metric;
    private int count;
    private String source;
    private Instant timestamp;
}