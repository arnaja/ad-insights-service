
package com.example.adinsights.dto;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ClicksResponse {
    private String campaignId;
    private int clicks;
    private Instant timestamp;
}