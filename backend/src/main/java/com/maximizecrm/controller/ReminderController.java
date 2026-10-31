package com.maximizecrm.controller;

import com.maximizecrm.dto.ReminderRequest;
import com.maximizecrm.dto.ReminderResponse;
import com.maximizecrm.entity.ReminderStatus;
import com.maximizecrm.service.ReminderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reminders")
public class ReminderController {

    private final ReminderService reminderService;

    public ReminderController(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    @PostMapping
    public ResponseEntity<ReminderResponse> createReminder(@Valid @RequestBody ReminderRequest request) {
        ReminderResponse created = reminderService.createReminder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<ReminderResponse>> getReminders(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) ReminderStatus status,
        @RequestParam(required = false) Long assignedUserId,
        @RequestParam(required = false) Long leadId
    ) {
        List<ReminderResponse> reminders = reminderService.getReminders(search, status, assignedUserId, leadId);
        return ResponseEntity.ok(reminders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReminderResponse> getReminderById(@PathVariable Long id) {
        ReminderResponse reminder = reminderService.getReminderById(id);
        return ResponseEntity.ok(reminder);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReminderResponse> updateReminder(
        @PathVariable Long id,
        @Valid @RequestBody ReminderRequest request
    ) {
        ReminderResponse updated = reminderService.updateReminder(id, request);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<ReminderResponse> completeReminder(@PathVariable Long id) {
        ReminderResponse completed = reminderService.completeReminder(id);
        return ResponseEntity.ok(completed);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteReminder(@PathVariable Long id) {
        reminderService.deleteReminder(id);
        return ResponseEntity.ok(Map.of(
            "status", 200,
            "message", "Reminder deleted successfully."
        ));
    }
}

