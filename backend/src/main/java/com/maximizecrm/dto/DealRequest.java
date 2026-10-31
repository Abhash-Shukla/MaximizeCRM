package com.maximizecrm.dto;

import com.maximizecrm.entity.DealStage;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record DealRequest(
    @NotBlank(message = "Deal name is required")
    @Size(max = 150, message = "Deal name must not exceed 150 characters")
    String name,

    @NotNull(message = "Lead ID is required")
    Long leadId,

    @NotNull(message = "Assigned user ID is required")
    Long assignedUserId,

    DealStage stage,

    @NotNull(message = "Deal amount is required")
    @DecimalMin(value = "0.01", message = "Deal amount must be greater than zero")
    BigDecimal amount,

    String notes
) {
}

