package com.maximizecrm.dto;

import com.maximizecrm.entity.LeadStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LeadRequest(
    @NotNull(message = "Contact ID is required")
    Long contactId,

    @NotNull(message = "Assigned user ID is required")
    Long assignedUserId,

    LeadStatus status,

    @NotBlank(message = "Lead source is required")
    @Size(max = 100, message = "Lead source must not exceed 100 characters")
    String source,

    String notes
) {
}

