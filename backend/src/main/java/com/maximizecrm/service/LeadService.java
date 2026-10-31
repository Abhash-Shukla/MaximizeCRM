package com.maximizecrm.service;

import com.maximizecrm.dto.LeadRequest;
import com.maximizecrm.dto.LeadResponse;
import com.maximizecrm.entity.*;
import com.maximizecrm.exception.ResourceNotFoundException;
import com.maximizecrm.repository.ContactRepository;
import com.maximizecrm.repository.LeadRepository;
import com.maximizecrm.repository.ReminderRepository;
import com.maximizecrm.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class LeadService {

    private final LeadRepository leadRepository;
    private final ContactRepository contactRepository;
    private final UserRepository userRepository;
    private final ReminderRepository reminderRepository;
    private final ReminderService reminderService;

    public LeadService(
        LeadRepository leadRepository,
        ContactRepository contactRepository,
        UserRepository userRepository,
        ReminderRepository reminderRepository,
        ReminderService reminderService
    ) {
        this.leadRepository = leadRepository;
        this.contactRepository = contactRepository;
        this.userRepository = userRepository;
        this.reminderRepository = reminderRepository;
        this.reminderService = reminderService;
    }

    public LeadResponse createLead(LeadRequest request) {
        Contact contact = contactRepository.findById(request.contactId())
            .orElseThrow(() -> new ResourceNotFoundException("Contact not found with ID: " + request.contactId()));

        User assignedUser = userRepository.findById(request.assignedUserId())
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + request.assignedUserId()));

        if (!assignedUser.isActive()) {
            throw new IllegalArgumentException("Cannot assign lead to an inactive user.");
        }

        LeadStatus status = request.status() != null ? request.status() : LeadStatus.NEW;
        String source = request.source() != null ? request.source().trim() : "";
        String notes = request.notes() != null && !request.notes().isBlank() ? request.notes().trim() : null;

        Lead lead = new Lead(contact, assignedUser, status, source, notes);
        Lead savedLead = leadRepository.save(lead);

        // Transactionally create exactly one follow-up reminder
        Reminder reminder = reminderService.createInitialLeadReminder(savedLead);

        return LeadResponse.fromEntity(savedLead, reminder);
    }

    @Transactional(readOnly = true)
    public List<LeadResponse> getLeads(String search, LeadStatus status, Long assignedUserId) {
        String cleanSearch = search != null && !search.trim().isEmpty() ? search.trim() : null;
        List<Lead> leads = leadRepository.searchLeads(cleanSearch, status, assignedUserId);

        return leads.stream()
            .map(lead -> {
                Reminder reminder = reminderRepository.findFirstByLeadIdOrderByCreatedAtDesc(lead.getId()).orElse(null);
                return LeadResponse.fromEntity(lead, reminder);
            })
            .toList();
    }

    @Transactional(readOnly = true)
    public LeadResponse getLeadById(Long id) {
        Lead lead = leadRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Lead not found with ID: " + id));

        Reminder reminder = reminderRepository.findFirstByLeadIdOrderByCreatedAtDesc(lead.getId()).orElse(null);
        return LeadResponse.fromEntity(lead, reminder);
    }

    public LeadResponse updateLead(Long id, LeadRequest request) {
        Lead lead = leadRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Lead not found with ID: " + id));

        if (request.contactId() != null && !request.contactId().equals(lead.getContact().getId())) {
            Contact contact = contactRepository.findById(request.contactId())
                .orElseThrow(() -> new ResourceNotFoundException("Contact not found with ID: " + request.contactId()));
            lead.setContact(contact);
        }

        if (request.assignedUserId() != null && !request.assignedUserId().equals(lead.getAssignedUser().getId())) {
            User assignedUser = userRepository.findById(request.assignedUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + request.assignedUserId()));
            if (!assignedUser.isActive()) {
                throw new IllegalArgumentException("Cannot assign lead to an inactive user.");
            }
            lead.setAssignedUser(assignedUser);
        }

        if (request.status() != null) {
            lead.setStatus(request.status());
        }

        if (request.source() != null && !request.source().isBlank()) {
            lead.setSource(request.source().trim());
        }

        lead.setNotes(request.notes() != null && !request.notes().isBlank() ? request.notes().trim() : null);

        Lead updated = leadRepository.save(lead);
        Reminder reminder = reminderRepository.findFirstByLeadIdOrderByCreatedAtDesc(updated.getId()).orElse(null);

        return LeadResponse.fromEntity(updated, reminder);
    }

    public void deleteLead(Long id) {
        Lead lead = leadRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Lead not found with ID: " + id));

        reminderRepository.deleteByLeadId(id);
        leadRepository.delete(lead);
    }
}

