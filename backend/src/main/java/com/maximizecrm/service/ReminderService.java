package com.maximizecrm.service;

import com.maximizecrm.dto.ReminderRequest;
import com.maximizecrm.dto.ReminderResponse;
import com.maximizecrm.entity.Lead;
import com.maximizecrm.entity.Reminder;
import com.maximizecrm.entity.ReminderStatus;
import com.maximizecrm.entity.User;
import com.maximizecrm.exception.ResourceNotFoundException;
import com.maximizecrm.repository.LeadRepository;
import com.maximizecrm.repository.ReminderRepository;
import com.maximizecrm.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ReminderService {

    private final ReminderRepository reminderRepository;
    private final UserRepository userRepository;
    private final LeadRepository leadRepository;

    public ReminderService(ReminderRepository reminderRepository) {
        this(reminderRepository, null, null);
    }

    @Autowired
    public ReminderService(
        ReminderRepository reminderRepository,
        UserRepository userRepository,
        LeadRepository leadRepository
    ) {
        this.reminderRepository = reminderRepository;
        this.userRepository = userRepository;
        this.leadRepository = leadRepository;
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

    public ReminderResponse createReminder(ReminderRequest request) {
        if (request.title() == null || request.title().trim().isEmpty()) {
            throw new IllegalArgumentException("Reminder title is required.");
        }

        User assignedUser = userRepository.findById(request.assignedUserId())
            .orElseThrow(() -> new ResourceNotFoundException("Assigned user not found with id: " + request.assignedUserId()));

        if (!assignedUser.isActive()) {
            throw new IllegalArgumentException("Cannot assign reminder to an inactive user.");
        }

        Lead lead = null;
        if (request.leadId() != null) {
            lead = leadRepository.findById(request.leadId())
                .orElseThrow(() -> new ResourceNotFoundException("Lead not found with id: " + request.leadId()));
        }

        Reminder reminder = new Reminder();
        reminder.setTitle(request.title().trim());
        reminder.setAssignedUser(assignedUser);
        reminder.setLead(lead);
        reminder.setStatus(request.status() != null ? request.status() : ReminderStatus.PENDING);
        reminder.setDueDate(request.dueDate());
        reminder.setNotes(request.notes());

        Reminder saved = reminderRepository.save(reminder);
        return ReminderResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<ReminderResponse> getReminders(String search, ReminderStatus status, Long assignedUserId, Long leadId) {
        String trimmedSearch = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
        List<Reminder> reminders = reminderRepository.searchReminders(trimmedSearch, status, assignedUserId, leadId);
        return reminders.stream().map(ReminderResponse::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public ReminderResponse getReminderById(Long id) {
        Reminder reminder = reminderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Reminder not found with id: " + id));
        return ReminderResponse.fromEntity(reminder);
    }

    public ReminderResponse updateReminder(Long id, ReminderRequest request) {
        Reminder reminder = reminderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Reminder not found with id: " + id));

        if (request.title() == null || request.title().trim().isEmpty()) {
            throw new IllegalArgumentException("Reminder title is required.");
        }

        User assignedUser = userRepository.findById(request.assignedUserId())
            .orElseThrow(() -> new ResourceNotFoundException("Assigned user not found with id: " + request.assignedUserId()));

        if (!assignedUser.isActive()) {
            throw new IllegalArgumentException("Cannot assign reminder to an inactive user.");
        }

        Lead lead = null;
        if (request.leadId() != null) {
            lead = leadRepository.findById(request.leadId())
                .orElseThrow(() -> new ResourceNotFoundException("Lead not found with id: " + request.leadId()));
        }

        reminder.setTitle(request.title().trim());
        reminder.setAssignedUser(assignedUser);
        reminder.setLead(lead);
        if (request.status() != null) {
            reminder.setStatus(request.status());
        }
        reminder.setDueDate(request.dueDate());
        reminder.setNotes(request.notes());

        Reminder updated = reminderRepository.save(reminder);
        return ReminderResponse.fromEntity(updated);
    }

    public ReminderResponse completeReminder(Long id) {
        Reminder reminder = reminderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Reminder not found with id: " + id));

        reminder.setStatus(ReminderStatus.COMPLETED);
        Reminder updated = reminderRepository.save(reminder);
        return ReminderResponse.fromEntity(updated);
    }

    public void deleteReminder(Long id) {
        Reminder reminder = reminderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Reminder not found with id: " + id));
        reminderRepository.delete(reminder);
    }
}
