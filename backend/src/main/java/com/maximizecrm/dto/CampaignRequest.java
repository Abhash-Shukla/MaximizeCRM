package com.maximizecrm.dto;

import com.maximizecrm.entity.CampaignStatus;
import com.maximizecrm.entity.CampaignType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CampaignRequest(
    @NotBlank(message = "Campaign name is required")
    @Size(max = 150, message = "Campaign name must not exceed 150 characters")
    String name,

    CampaignType type,

    CampaignStatus status,

    LocalDate startDate,

    LocalDate endDate,

    @DecimalMin(value = "0.00", message = "Budget must be greater than or equal to zero")
    BigDecimal budget,

    @DecimalMin(value = "0.00", message = "Expected revenue must be greater than or equal to zero")
    BigDecimal expectedRevenue,

    @DecimalMin(value = "0.00", message = "Actual cost must be greater than or equal to zero")
    BigDecimal actualCost,

    @NotNull(message = "Assigned user ID is required")
    Long assignedUserId,

    String description
) {
}

