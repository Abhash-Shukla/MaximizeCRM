package com.maximizecrm.dto;

import com.maximizecrm.entity.Lead;
import com.maximizecrm.entity.LeadStatus;
import com.maximizecrm.entity.Reminder;

import java.time.LocalDateTime;

public record LeadResponse(
    Long id,
    Long contactId,
    String contactName,
    String contactEmail,
    String contactPhone,
    String contactCompany,
    Long assignedUserId,
    String assignedUserName,
    String assignedUserEmail,
    LeadStatus status,
    String source,
    String notes,
    Long reminderId,
    String reminderStatus,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static LeadResponse fromEntity(Lead lead, Reminder reminder) {
        if (lead == null) {
            return null;
        }

        Long reminderId = reminder != null ? reminder.getId() : null;
        String reminderStatus = reminder != null && reminder.getStatus() != null ? reminder.getStatus().name() : null;

        return new LeadResponse(
            lead.getId(),
            lead.getContact() != null ? lead.getContact().getId() : null,
            lead.getContact() != null ? lead.getContact().getName() : null,
            lead.getContact() != null ? lead.getContact().getEmail() : null,
            lead.getContact() != null ? lead.getContact().getPhone() : null,
            lead.getContact() != null ? lead.getContact().getCompany() : null,
            lead.getAssignedUser() != null ? lead.getAssignedUser().getId() : null,
            lead.getAssignedUser() != null ? lead.getAssignedUser().getFullName() : null,
            lead.getAssignedUser() != null ? lead.getAssignedUser().getEmail() : null,
            lead.getStatus(),
            lead.getSource(),
            lead.getNotes(),
            reminderId,
            reminderStatus,
            lead.getCreatedAt(),
            lead.getUpdatedAt()
        );
    }
}

