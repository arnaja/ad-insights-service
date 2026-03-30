package com.adinsights.controller;

import com.adinsights.dto.MetricResponse;
import com.adinsights.exception.AdInsightsException;
import com.adinsights.exception.ValidationException;
import com.adinsights.service.AdInsightsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

import static com.adinsights.util.LogUtils.kv;

@RestController
@RequestMapping("/v1/ad")
@RequiredArgsConstructor
@Tag(name = "Ad Insights API", description = "APIs for ad performance insights")
public class AdInsightsController {

    private static final Logger log =
            LoggerFactory.getLogger(AdInsightsController.class);

    private final AdInsightsService service;

    @Operation(summary = "Get clicks for campaign")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful response"),
            @ApiResponse(responseCode = "500", description = "Internal error")
    })
    @GetMapping("/{campaignId}/clicks")
    public ResponseEntity<MetricResponse> getClicks(
            @Parameter(description = "Tenant ID", required = true)
            @RequestHeader("X-Tenant-Id") String tenantId,

            @Parameter(description = "Campaign ID", required = true)
            @PathVariable String campaignId,

            @Parameter(description = "Start time (ISO-8601)")
            @RequestParam Instant start,

            @Parameter(description = "End time (ISO-8601)")
            @RequestParam Instant end) {

        try {
            log.info("Clicks API request received",
                    kv("tenantId", tenantId),
                    kv("campaignId", campaignId),
                    kv("start", start),
                    kv("end", end));

            if (start.isAfter(end)) {
                throw new ValidationException("Start time cannot be after end time");
            }

            MetricResponse response =
                    service.getClicks(tenantId, campaignId, start, end);

            log.info("Clicks API response success",
                    kv("tenantId", tenantId),
                    kv("campaignId", campaignId),
                    kv("result", response.getCount()));

            return ResponseEntity.ok(response);

        } catch (AdInsightsException ex) {
            log.error("Clicks API failure",
                    kv("tenantId", tenantId),
                    kv("campaignId", campaignId),
                    ex);
            throw ex; // handled globally
        } catch (Exception ex) {
            log.error("Clicks API failure",
                    kv("tenantId", tenantId),
                    kv("campaignId", campaignId),
                    ex);
            throw new AdInsightsException("Failed to fetch clicks", "API_ERROR");
        }
    }
}