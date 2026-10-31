package com.maximizecrm.service;

import com.maximizecrm.entity.Lead;
import com.maximizecrm.entity.Reminder;
import com.maximizecrm.entity.ReminderStatus;
import com.maximizecrm.repository.ReminderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
public class ReminderService {

    private final ReminderRepository reminderRepository;

    public ReminderService(ReminderRepository reminderRepository) {
        this.reminderRepository = reminderRepository;
    }

    public Reminder createInitialLeadReminder(Lead lead) {
        if (lead == null || lead.getId() == null) {
            throw new IllegalArgumentException("Lead must be persisted before creating an automatic reminder.");
        }

        // Avoid duplicate reminders if request is repeated or retried
        Optional<Reminder> existing = reminderRepository.findByLeadId(lead.getId());
        if (existing.isPresent()) {
            return existing.get();
        }

        String contactName = lead.getContact() != null ? lead.getContact().getName() : "Lead #" + lead.getId();
        Reminder reminder = new Reminder(
            "Follow-up with " + contactName,
            lead,
            lead.getAssignedUser(),
            ReminderStatus.PENDING,
            LocalDateTime.now().plusDays(1),
            "Automated follow-up reminder for lead #" + lead.getId() + " (" + lead.getSource() + ")"
        );

        return reminderRepository.save(reminder);
    }

    @Transactional(readOnly = true)
    public Optional<Reminder> getReminderByLeadId(Long leadId) {
        return reminderRepository.findByLeadId(leadId);
    }
}

