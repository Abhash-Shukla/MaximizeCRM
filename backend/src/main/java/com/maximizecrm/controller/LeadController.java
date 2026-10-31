package com.maximizecrm.controller;

import com.maximizecrm.dto.LeadRequest;
import com.maximizecrm.dto.LeadResponse;
import com.maximizecrm.entity.LeadStatus;
import com.maximizecrm.service.LeadService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/leads")
public class LeadController {

    private final LeadService leadService;

    public LeadController(LeadService leadService) {
        this.leadService = leadService;
    }

    @PostMapping
    public ResponseEntity<LeadResponse> createLead(@Valid @RequestBody LeadRequest request) {
        LeadResponse created = leadService.createLead(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<LeadResponse>> getLeads(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) LeadStatus status,
        @RequestParam(required = false) Long assignedUserId
    ) {
        List<LeadResponse> leads = leadService.getLeads(search, status, assignedUserId);
        return ResponseEntity.ok(leads);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LeadResponse> getLeadById(@PathVariable Long id) {
        LeadResponse lead = leadService.getLeadById(id);
        return ResponseEntity.ok(lead);
    }

    @PutMapping("/{id}")
    public ResponseEntity<LeadResponse> updateLead(
        @PathVariable Long id,
        @Valid @RequestBody LeadRequest request
    ) {
        LeadResponse updated = leadService.updateLead(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteLead(@PathVariable Long id) {
        leadService.deleteLead(id);
        return ResponseEntity.ok(Map.of(
            "status", HttpStatus.OK.value(),
            "message", "Lead deleted successfully"
        ));
    }
}

