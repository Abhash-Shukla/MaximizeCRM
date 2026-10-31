package com.maximizecrm.controller;

import com.maximizecrm.dto.CampaignRequest;
import com.maximizecrm.dto.CampaignResponse;
import com.maximizecrm.entity.CampaignStatus;
import com.maximizecrm.service.CampaignService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/campaigns")
public class CampaignController {

    private final CampaignService campaignService;

    public CampaignController(CampaignService campaignService) {
        this.campaignService = campaignService;
    }

    @PostMapping
    public ResponseEntity<CampaignResponse> createCampaign(@Valid @RequestBody CampaignRequest request) {
        CampaignResponse created = campaignService.createCampaign(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<CampaignResponse>> getCampaigns(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) CampaignStatus status,
        @RequestParam(required = false) Long assignedUserId
    ) {
        List<CampaignResponse> campaigns = campaignService.getCampaigns(search, status, assignedUserId);
        return ResponseEntity.ok(campaigns);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CampaignResponse> getCampaignById(@PathVariable Long id) {
        CampaignResponse campaign = campaignService.getCampaignById(id);
        return ResponseEntity.ok(campaign);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CampaignResponse> updateCampaign(
        @PathVariable Long id,
        @Valid @RequestBody CampaignRequest request
    ) {
        CampaignResponse updated = campaignService.updateCampaign(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteCampaign(@PathVariable Long id) {
        campaignService.deleteCampaign(id);
        return ResponseEntity.ok(Map.of(
            "status", HttpStatus.OK.value(),
            "message", "Campaign deleted successfully"
        ));
    }
}

