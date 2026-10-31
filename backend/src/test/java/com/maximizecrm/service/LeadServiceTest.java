package com.maximizecrm.service;

import com.maximizecrm.dto.LeadRequest;
import com.maximizecrm.dto.LeadResponse;
import com.maximizecrm.entity.*;
import com.maximizecrm.exception.ResourceNotFoundException;
import com.maximizecrm.repository.ContactRepository;
import com.maximizecrm.repository.LeadRepository;
import com.maximizecrm.repository.ReminderRepository;
import com.maximizecrm.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeadServiceTest {

    @Mock
    private LeadRepository leadRepository;

    @Mock
    private ContactRepository contactRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReminderRepository reminderRepository;

    private ReminderService reminderService;
    private LeadService leadService;

    private Contact sampleContact;
    private User sampleUser;

    @BeforeEach
    void setUp() {
        reminderService = new ReminderService(reminderRepository);
        leadService = new LeadService(
            leadRepository,
            contactRepository,
            userRepository,
            reminderRepository,
            reminderService
        );

        sampleContact = new Contact("Acme Client", "client@acme.com", "+1-555-1111", ContactCategory.VIP, "Acme", null);
        sampleContact.setId(10L);

        sampleUser = new User("John Sales", "john@maximizecrm.local", "hash", Role.SALES_REP, true);
        sampleUser.setId(20L);
    }

    @Test
    @DisplayName("createLead should save lead and automatically create exactly one PENDING reminder")
    void createLead_Success_CreatesLeadAndPendingReminder() {
        LeadRequest request = new LeadRequest(10L, 20L, LeadStatus.NEW, "Website", "High interest in enterprise tier");

        Lead savedLead = new Lead(sampleContact, sampleUser, LeadStatus.NEW, "Website", "High interest in enterprise tier");
        savedLead.setId(100L);

        Reminder savedReminder = new Reminder(
            "Follow-up with Acme Client",
            savedLead,
            sampleUser,
            ReminderStatus.PENDING,
            LocalDateTime.now().plusDays(1),
            "Automated follow-up reminder for lead #100 (Website)"
        );
        savedReminder.setId(500L);

        when(contactRepository.findById(10L)).thenReturn(Optional.of(sampleContact));
        when(userRepository.findById(20L)).thenReturn(Optional.of(sampleUser));
        when(leadRepository.save(any(Lead.class))).thenReturn(savedLead);
        when(reminderRepository.findByLeadId(100L)).thenReturn(Optional.empty());
        when(reminderRepository.save(any(Reminder.class))).thenReturn(savedReminder);

        LeadResponse response = leadService.createLead(request);

        assertNotNull(response);
        assertEquals(100L, response.id());
        assertEquals("Acme Client", response.contactName());
        assertEquals(10L, response.contactId());
        assertEquals(20L, response.assignedUserId());
        assertEquals("John Sales", response.assignedUserName());
        assertEquals(LeadStatus.NEW, response.status());
        assertEquals("Website", response.source());
        assertEquals("High interest in enterprise tier", response.notes());

        // Verify reminder fields
        assertEquals(500L, response.reminderId());
        assertEquals("PENDING", response.reminderStatus());

        // Verify reminder was persisted with status PENDING and assigned to the same user
        ArgumentCaptor<Reminder> reminderCaptor = ArgumentCaptor.forClass(Reminder.class);
        verify(reminderRepository).save(reminderCaptor.capture());
        Reminder captured = reminderCaptor.getValue();
        assertEquals(ReminderStatus.PENDING, captured.getStatus());
        assertEquals(sampleUser, captured.getAssignedUser());
        assertEquals(savedLead, captured.getLead());
    }

    @Test
    @DisplayName("createLead should avoid duplicate reminders if lead reminder already exists")
    void createLead_AvoidsDuplicateReminderWhenRetried() {
        LeadRequest request = new LeadRequest(10L, 20L, LeadStatus.NEW, "Referral", null);

        Lead savedLead = new Lead(sampleContact, sampleUser, LeadStatus.NEW, "Referral", null);
        savedLead.setId(101L);

        Reminder existingReminder = new Reminder(
            "Follow-up with Acme Client",
            savedLead,
            sampleUser,
            ReminderStatus.PENDING,
            LocalDateTime.now().plusDays(1),
            "Existing"
        );
        existingReminder.setId(501L);

        when(contactRepository.findById(10L)).thenReturn(Optional.of(sampleContact));
        when(userRepository.findById(20L)).thenReturn(Optional.of(sampleUser));
        when(leadRepository.save(any(Lead.class))).thenReturn(savedLead);
        // Simulate existing reminder
        when(reminderRepository.findByLeadId(101L)).thenReturn(Optional.of(existingReminder));

        LeadResponse response = leadService.createLead(request);

        assertNotNull(response);
        assertEquals(501L, response.reminderId());
        // Verify save was NOT called again on reminderRepository
        verify(reminderRepository, never()).save(any(Reminder.class));
    }

    @Test
    @DisplayName("createLead should throw ResourceNotFoundException when contact not found")
    void createLead_ContactNotFound_ThrowsException() {
        LeadRequest request = new LeadRequest(999L, 20L, LeadStatus.NEW, "Phone", null);
        when(contactRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> leadService.createLead(request));
        verify(leadRepository, never()).save(any());
        verify(reminderRepository, never()).save(any());
    }

    @Test
    @DisplayName("createLead should throw ResourceNotFoundException when assigned user not found")
    void createLead_UserNotFound_ThrowsException() {
        LeadRequest request = new LeadRequest(10L, 999L, LeadStatus.NEW, "Phone", null);
        when(contactRepository.findById(10L)).thenReturn(Optional.of(sampleContact));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> leadService.createLead(request));
        verify(leadRepository, never()).save(any());
        verify(reminderRepository, never()).save(any());
    }

    @Test
    @DisplayName("createLead should throw IllegalArgumentException when assigned user is inactive")
    void createLead_InactiveUser_ThrowsException() {
        sampleUser.setActive(false);
        LeadRequest request = new LeadRequest(10L, 20L, LeadStatus.NEW, "Phone", null);

        when(contactRepository.findById(10L)).thenReturn(Optional.of(sampleContact));
        when(userRepository.findById(20L)).thenReturn(Optional.of(sampleUser));

        assertThrows(IllegalArgumentException.class, () -> leadService.createLead(request));
        verify(leadRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateLead should successfully update status, source, notes and assigned user")
    void updateLead_Success() {
        Lead existing = new Lead(sampleContact, sampleUser, LeadStatus.NEW, "Website", "Old note");
        existing.setId(200L);

        User newRep = new User("Jane Sales", "jane@maximizecrm.local", "hash", Role.SALES_REP, true);
        newRep.setId(21L);

        LeadRequest updateRequest = new LeadRequest(10L, 21L, LeadStatus.CONTACTED, "Walk-in", "Met in person");

        when(leadRepository.findById(200L)).thenReturn(Optional.of(existing));
        when(userRepository.findById(21L)).thenReturn(Optional.of(newRep));
        when(leadRepository.save(existing)).thenReturn(existing);
        when(reminderRepository.findFirstByLeadIdOrderByCreatedAtDesc(200L)).thenReturn(Optional.empty());

        LeadResponse response = leadService.updateLead(200L, updateRequest);

        assertNotNull(response);
        assertEquals(LeadStatus.CONTACTED, response.status());
        assertEquals("Walk-in", response.source());
        assertEquals("Met in person", response.notes());
        assertEquals(21L, response.assignedUserId());
    }

    @Test
    @DisplayName("deleteLead should delete reminder and lead")
    void deleteLead_Success() {
        Lead existing = new Lead(sampleContact, sampleUser, LeadStatus.NEW, "Website", null);
        existing.setId(300L);

        when(leadRepository.findById(300L)).thenReturn(Optional.of(existing));

        leadService.deleteLead(300L);

        verify(reminderRepository).deleteByLeadId(300L);
        verify(leadRepository).delete(existing);
    }
}

