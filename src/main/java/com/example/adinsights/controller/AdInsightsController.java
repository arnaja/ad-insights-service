
package com.example.adinsights.controller;

import com.example.adinsights.dto.ClicksResponse;
import com.example.adinsights.service.AdInsightsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ad")
@RequiredArgsConstructor
public class AdInsightsController {

    private final AdInsightsService adInsightsService;

    @GetMapping("/{campaignId}/clicks")
    public ResponseEntity<ClicksResponse> getClicks(
            @PathVariable String campaignId,
            @RequestHeader("X-Tenant-ID") String tenantId) {

        ClicksResponse response = adInsightsService.getClicks(campaignId, tenantId);
        return ResponseEntity.ok(response);
    }
}