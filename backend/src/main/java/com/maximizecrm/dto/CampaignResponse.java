package com.maximizecrm.dto;

import com.maximizecrm.entity.Campaign;
import com.maximizecrm.entity.CampaignStatus;
import com.maximizecrm.entity.CampaignType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record CampaignResponse(
    Long id,
    String name,
    CampaignType type,
    CampaignStatus status,
    LocalDate startDate,
    LocalDate endDate,
    BigDecimal budget,
    BigDecimal expectedRevenue,
    BigDecimal actualCost,
    Long assignedUserId,
    String assignedUserName,
    String assignedUserEmail,
    String description,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static CampaignResponse fromEntity(Campaign campaign) {
        if (campaign == null) {
            return null;
        }

        Long assignedUserId = campaign.getAssignedUser() != null ? campaign.getAssignedUser().getId() : null;
        String assignedUserName = campaign.getAssignedUser() != null ? campaign.getAssignedUser().getFullName() : null;
        String assignedUserEmail = campaign.getAssignedUser() != null ? campaign.getAssignedUser().getEmail() : null;

        return new CampaignResponse(
            campaign.getId(),
            campaign.getName(),
            campaign.getType(),
            campaign.getStatus(),
            campaign.getStartDate(),
            campaign.getEndDate(),
            campaign.getBudget(),
            campaign.getExpectedRevenue(),
            campaign.getActualCost(),
            assignedUserId,
            assignedUserName,
            assignedUserEmail,
            campaign.getDescription(),
            campaign.getCreatedAt(),
            campaign.getUpdatedAt()
        );
    }
}

