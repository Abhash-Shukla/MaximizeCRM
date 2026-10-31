package com.maximizecrm.dto;

import com.maximizecrm.entity.Deal;
import com.maximizecrm.entity.DealStage;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DealResponse(
    Long id,
    String name,
    Long leadId,
    String leadSource,
    String leadStatus,
    Long contactId,
    String contactName,
    String contactEmail,
    String contactPhone,
    String contactCompany,
    Long assignedUserId,
    String assignedUserName,
    String assignedUserEmail,
    DealStage stage,
    BigDecimal amount,
    String notes,
    LocalDateTime closedAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static DealResponse fromEntity(Deal deal) {
        if (deal == null) {
            return null;
        }

        Long leadId = deal.getLead() != null ? deal.getLead().getId() : null;
        String leadSource = deal.getLead() != null ? deal.getLead().getSource() : null;
        String leadStatus = deal.getLead() != null && deal.getLead().getStatus() != null
            ? deal.getLead().getStatus().name() : null;

        Long contactId = null;
        String contactName = null;
        String contactEmail = null;
        String contactPhone = null;
        String contactCompany = null;

        if (deal.getLead() != null && deal.getLead().getContact() != null) {
            contactId = deal.getLead().getContact().getId();
            contactName = deal.getLead().getContact().getName();
            contactEmail = deal.getLead().getContact().getEmail();
            contactPhone = deal.getLead().getContact().getPhone();
            contactCompany = deal.getLead().getContact().getCompany();
        }

        Long assignedUserId = deal.getAssignedUser() != null ? deal.getAssignedUser().getId() : null;
        String assignedUserName = deal.getAssignedUser() != null ? deal.getAssignedUser().getFullName() : null;
        String assignedUserEmail = deal.getAssignedUser() != null ? deal.getAssignedUser().getEmail() : null;

        return new DealResponse(
            deal.getId(),
            deal.getName(),
            leadId,
            leadSource,
            leadStatus,
            contactId,
            contactName,
            contactEmail,
            contactPhone,
            contactCompany,
            assignedUserId,
            assignedUserName,
            assignedUserEmail,
            deal.getStage(),
            deal.getAmount(),
            deal.getNotes(),
            deal.getClosedAt(),
            deal.getCreatedAt(),
            deal.getUpdatedAt()
        );
    }
}

