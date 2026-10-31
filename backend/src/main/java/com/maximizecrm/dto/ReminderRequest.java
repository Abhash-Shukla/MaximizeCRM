package com.maximizecrm.dto;

import com.maximizecrm.entity.ReminderStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record ReminderRequest(
    @NotBlank(message = "Reminder title is required")
    @Size(max = 200, message = "Reminder title must not exceed 200 characters")
    String title,

    LocalDateTime dueDate,

    Long leadId,

    @NotNull(message = "Assigned user ID is required")
    Long assignedUserId,

    ReminderStatus status,

    String notes
) {
}

