package com.maximizecrm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.maximizecrm.config.GlobalExceptionHandler;
import com.maximizecrm.dto.ReminderRequest;
import com.maximizecrm.dto.ReminderResponse;
import com.maximizecrm.entity.ReminderStatus;
import com.maximizecrm.service.ReminderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ReminderControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ReminderService reminderService;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @BeforeEach
    void setUp() {
        ReminderController reminderController = new ReminderController(reminderService);
        mockMvc = MockMvcBuilders.standaloneSetup(reminderController)
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
    }

    @Test
    @DisplayName("GET /api/reminders should return 200 with reminder list")
    void getReminders_Returns200() throws Exception {
        ReminderResponse response = new ReminderResponse(
            1L,
            "Follow-up with client",
            10L,
            "Alice Client",
            "Referral",
            100L,
            20L,
            "John Rep",
            "rep@maximizecrm.local",
            ReminderStatus.PENDING,
            LocalDateTime.of(2026, 6, 1, 10, 0),
            "Call at 10 AM",
            false,
            LocalDateTime.now(),
            null
        );

        when(reminderService.getReminders(any(), any(), any(), any())).thenReturn(List.of(response));

        mockMvc.perform(get("/api/reminders"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].title").value("Follow-up with client"))
            .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    @DisplayName("POST /api/reminders should return 201 when request is valid")
    void createReminder_ValidRequest_Returns201() throws Exception {
        ReminderRequest request = new ReminderRequest(
            "Schedule onboarding call",
            LocalDateTime.of(2026, 6, 2, 14, 0),
            10L,
            20L,
            ReminderStatus.PENDING,
            "Prep slide deck"
        );

        ReminderResponse response = new ReminderResponse(
            2L,
            request.title(),
            request.leadId(),
            "Bob Prospect",
            "Website",
            101L,
            request.assignedUserId(),
            "John Rep",
            "rep@maximizecrm.local",
            ReminderStatus.PENDING,
            request.dueDate(),
            request.notes(),
            false,
            LocalDateTime.now(),
            null
        );

        when(reminderService.createReminder(any(ReminderRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/reminders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(2))
            .andExpect(jsonPath("$.title").value("Schedule onboarding call"));
    }

    @Test
    @DisplayName("POST /api/reminders with blank title should return 400 Bad Request")
    void createReminder_BlankTitle_Returns400() throws Exception {
        ReminderRequest request = new ReminderRequest(
            "   ",
            LocalDateTime.now(),
            null,
            20L,
            ReminderStatus.PENDING,
            null
        );

        mockMvc.perform(post("/api/reminders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /api/reminders/{id}/complete should return 200 with completed status")
    void completeReminder_Returns200() throws Exception {
        ReminderResponse completed = new ReminderResponse(
            5L,
            "Product review",
            null,
            null,
            null,
            null,
            20L,
            "John Rep",
            "rep@maximizecrm.local",
            ReminderStatus.COMPLETED,
            LocalDateTime.of(2026, 6, 1, 9, 0),
            "Finished",
            false,
            LocalDateTime.now(),
            LocalDateTime.now()
        );

        when(reminderService.completeReminder(5L)).thenReturn(completed);

        mockMvc.perform(patch("/api/reminders/5/complete"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @DisplayName("DELETE /api/reminders/{id} should return 200 with success message")
    void deleteReminder_Returns200() throws Exception {
        doNothing().when(reminderService).deleteReminder(10L);

        mockMvc.perform(delete("/api/reminders/10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.message").value("Reminder deleted successfully."));
    }
}

