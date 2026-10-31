package com.maximizecrm.controller;

import com.maximizecrm.dto.DealRequest;
import com.maximizecrm.dto.DealResponse;
import com.maximizecrm.dto.LeadResponse;
import com.maximizecrm.entity.DealStage;
import com.maximizecrm.service.DealService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/deals")
public class DealController {

    private final DealService dealService;

    public DealController(DealService dealService) {
        this.dealService = dealService;
    }

    @PostMapping
    public ResponseEntity<DealResponse> createDeal(@Valid @RequestBody DealRequest request) {
        DealResponse created = dealService.createDeal(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<DealResponse>> getDeals(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) DealStage stage,
        @RequestParam(required = false) Long assignedUserId
    ) {
        List<DealResponse> deals = dealService.getDeals(search, stage, assignedUserId);
        return ResponseEntity.ok(deals);
    }

    @GetMapping("/available-leads")
    public ResponseEntity<List<LeadResponse>> getAvailableLeads() {
        List<LeadResponse> availableLeads = dealService.getAvailableLeads();
        return ResponseEntity.ok(availableLeads);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DealResponse> getDealById(@PathVariable Long id) {
        DealResponse deal = dealService.getDealById(id);
        return ResponseEntity.ok(deal);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DealResponse> updateDeal(
        @PathVariable Long id,
        @Valid @RequestBody DealRequest request
    ) {
        DealResponse updated = dealService.updateDeal(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteDeal(@PathVariable Long id) {
        dealService.deleteDeal(id);
        return ResponseEntity.ok(Map.of(
            "status", HttpStatus.OK.value(),
            "message", "Deal deleted successfully"
        ));
    }
}

