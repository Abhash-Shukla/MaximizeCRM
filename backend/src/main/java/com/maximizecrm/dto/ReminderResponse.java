package com.maximizecrm.dto;

import com.maximizecrm.entity.Reminder;
import com.maximizecrm.entity.ReminderStatus;

import java.time.LocalDateTime;

public record ReminderResponse(
    Long id,
    String title,
    Long leadId,
    String leadContactName,
    String leadSource,
    Long contactId,
    Long assignedUserId,
    String assignedUserName,
    String assignedUserEmail,
    ReminderStatus status,
    LocalDateTime dueDate,
    String notes,
    boolean overdue,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static ReminderResponse fromEntity(Reminder reminder) {
        if (reminder == null) {
            return null;
        }

        Long leadId = reminder.getLead() != null ? reminder.getLead().getId() : null;
        String leadContactName = null;
        String leadSource = null;
        Long contactId = null;

        if (reminder.getLead() != null) {
            leadSource = reminder.getLead().getSource();
            if (reminder.getLead().getContact() != null) {
                leadContactName = reminder.getLead().getContact().getName();
                contactId = reminder.getLead().getContact().getId();
            }
        }

        Long assignedUserId = reminder.getAssignedUser() != null ? reminder.getAssignedUser().getId() : null;
        String assignedUserName = reminder.getAssignedUser() != null ? reminder.getAssignedUser().getFullName() : null;
        String assignedUserEmail = reminder.getAssignedUser() != null ? reminder.getAssignedUser().getEmail() : null;

        boolean overdue = reminder.getStatus() == ReminderStatus.PENDING
            && reminder.getDueDate() != null
            && reminder.getDueDate().isBefore(LocalDateTime.now());

        return new ReminderResponse(
            reminder.getId(),
            reminder.getTitle(),
            leadId,
            leadContactName,
            leadSource,
            contactId,
            assignedUserId,
            assignedUserName,
            assignedUserEmail,
            reminder.getStatus(),
            reminder.getDueDate(),
            reminder.getNotes(),
            overdue,
            reminder.getCreatedAt(),
            reminder.getUpdatedAt()
        );
    }
}

