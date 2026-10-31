package com.maximizecrm.service;

import com.maximizecrm.dto.ReminderRequest;
import com.maximizecrm.dto.ReminderResponse;
import com.maximizecrm.entity.*;
import com.maximizecrm.exception.ResourceNotFoundException;
import com.maximizecrm.repository.LeadRepository;
import com.maximizecrm.repository.ReminderRepository;
import com.maximizecrm.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReminderServiceTest {

    @Mock
    private ReminderRepository reminderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LeadRepository leadRepository;

    private ReminderService reminderService;
    private User sampleUser;
    private Lead sampleLead;
    private Contact sampleContact;

    @BeforeEach
    void setUp() {
        reminderService = new ReminderService(reminderRepository, userRepository, leadRepository);

        sampleUser = new User("Rep User", "rep@maximizecrm.local", "hash", Role.SALES_REP, true);
        sampleUser.setId(20L);

        sampleContact = new Contact("Acme Client", "client@acme.com", "+1-555-0199", ContactCategory.REGULAR, "Acme Corp", null);
        sampleContact.setId(30L);

        sampleLead = new Lead(sampleContact, sampleUser, LeadStatus.NEW, "Website", "Interested in demo");
        sampleLead.setId(40L);
    }

    @Test
    @DisplayName("createReminder should successfully create and return reminder")
    void createReminder_Success() {
        ReminderRequest request = new ReminderRequest(
            "Call back Acme Client",
            LocalDateTime.of(2026, 6, 15, 14, 0),
            40L,
            20L,
            ReminderStatus.PENDING,
            "Discuss pricing proposal"
        );

        when(userRepository.findById(20L)).thenReturn(Optional.of(sampleUser));
        when(leadRepository.findById(40L)).thenReturn(Optional.of(sampleLead));
        when(reminderRepository.save(any(Reminder.class))).thenAnswer(invocation -> {
            Reminder r = invocation.getArgument(0);
            r.setId(100L);
            return r;
        });

        ReminderResponse response = reminderService.createReminder(request);

        assertNotNull(response);
        assertEquals(100L, response.id());
        assertEquals("Call back Acme Client", response.title());
        assertEquals(ReminderStatus.PENDING, response.status());
        assertEquals(40L, response.leadId());
        assertEquals("Acme Client", response.leadContactName());
        assertEquals(20L, response.assignedUserId());
        verify(reminderRepository).save(any(Reminder.class));
    }

    @Test
    @DisplayName("createReminder without lead should succeed when leadId is null")
    void createReminder_WithoutLead_Success() {
        ReminderRequest request = new ReminderRequest(
            "Quarterly tax prep",
            LocalDateTime.of(2026, 7, 1, 9, 0),
            null,
            20L,
            ReminderStatus.PENDING,
            "Internal admin reminder"
        );

        when(userRepository.findById(20L)).thenReturn(Optional.of(sampleUser));
        when(reminderRepository.save(any(Reminder.class))).thenAnswer(invocation -> {
            Reminder r = invocation.getArgument(0);
            r.setId(101L);
            return r;
        });

        ReminderResponse response = reminderService.createReminder(request);

        assertNotNull(response);
        assertEquals(101L, response.id());
        assertNull(response.leadId());
        verify(leadRepository, never()).findById(any());
    }

    @Test
    @DisplayName("createReminder should throw when title is blank")
    void createReminder_BlankTitle_ThrowsException() {
        ReminderRequest request = new ReminderRequest(
            "   ",
            LocalDateTime.now(),
            null,
            20L,
            ReminderStatus.PENDING,
            null
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> reminderService.createReminder(request));
        assertTrue(ex.getMessage().contains("Reminder title is required"));
    }

    @Test
    @DisplayName("createReminder should throw when assigned user not found")
    void createReminder_UserNotFound_ThrowsException() {
        ReminderRequest request = new ReminderRequest(
            "Follow-up",
            LocalDateTime.now(),
            null,
            999L,
            ReminderStatus.PENDING,
            null
        );

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> reminderService.createReminder(request));
    }

    @Test
    @DisplayName("createReminder should throw when assigned user is inactive")
    void createReminder_InactiveUser_ThrowsException() {
        sampleUser.setActive(false);
        ReminderRequest request = new ReminderRequest(
            "Follow-up",
            LocalDateTime.now(),
            null,
            20L,
            ReminderStatus.PENDING,
            null
        );

        when(userRepository.findById(20L)).thenReturn(Optional.of(sampleUser));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> reminderService.createReminder(request));
        assertTrue(ex.getMessage().contains("inactive"));
    }

    @Test
    @DisplayName("updateReminder should successfully update reminder fields")
    void updateReminder_Success() {
        Reminder existing = new Reminder("Initial follow-up", sampleLead, sampleUser, ReminderStatus.PENDING, LocalDateTime.now(), null);
        existing.setId(50L);

        ReminderRequest updateRequest = new ReminderRequest(
            "Updated follow-up topic",
            LocalDateTime.of(2026, 8, 1, 10, 0),
            40L,
            20L,
            ReminderStatus.PENDING,
            "Detailed agenda prepared"
        );

        when(reminderRepository.findById(50L)).thenReturn(Optional.of(existing));
        when(userRepository.findById(20L)).thenReturn(Optional.of(sampleUser));
        when(leadRepository.findById(40L)).thenReturn(Optional.of(sampleLead));
        when(reminderRepository.save(any(Reminder.class))).thenAnswer(inv -> inv.getArgument(0));

        ReminderResponse response = reminderService.updateReminder(50L, updateRequest);

        assertNotNull(response);
        assertEquals("Updated follow-up topic", response.title());
        assertEquals("Detailed agenda prepared", response.notes());
    }

    @Test
    @DisplayName("completeReminder should set status to COMPLETED")
    void completeReminder_Success() {
        Reminder existing = new Reminder("Task to complete", sampleLead, sampleUser, ReminderStatus.PENDING, LocalDateTime.now(), null);
        existing.setId(60L);

        when(reminderRepository.findById(60L)).thenReturn(Optional.of(existing));
        when(reminderRepository.save(any(Reminder.class))).thenAnswer(inv -> inv.getArgument(0));

        ReminderResponse response = reminderService.completeReminder(60L);

        assertNotNull(response);
        assertEquals(ReminderStatus.COMPLETED, response.status());
        assertFalse(response.overdue());
    }

    @Test
    @DisplayName("deleteReminder should remove reminder when found")
    void deleteReminder_Success() {
        Reminder existing = new Reminder("Task to remove", sampleLead, sampleUser, ReminderStatus.PENDING, LocalDateTime.now(), null);
        existing.setId(70L);

        when(reminderRepository.findById(70L)).thenReturn(Optional.of(existing));

        reminderService.deleteReminder(70L);

        verify(reminderRepository).delete(existing);
    }

    @Test
    @DisplayName("getReminders should query repository with filters")
    void getReminders_FiltersApplied() {
        Reminder r = new Reminder("Meeting reminder", sampleLead, sampleUser, ReminderStatus.PENDING, LocalDateTime.now(), null);
        r.setId(80L);

        when(reminderRepository.searchReminders("Meeting", ReminderStatus.PENDING, 20L, 40L))
            .thenReturn(List.of(r));

        List<ReminderResponse> results = reminderService.getReminders("Meeting", ReminderStatus.PENDING, 20L, 40L);

        assertEquals(1, results.size());
        assertEquals("Meeting reminder", results.get(0).title());
    }
}
