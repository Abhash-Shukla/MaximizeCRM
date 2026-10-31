package com.maximizecrm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.maximizecrm.config.GlobalExceptionHandler;
import com.maximizecrm.dto.LeadRequest;
import com.maximizecrm.dto.LeadResponse;
import com.maximizecrm.entity.LeadStatus;
import com.maximizecrm.exception.ResourceNotFoundException;
import com.maximizecrm.service.LeadService;
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
class LeadControllerTest {

    private MockMvc mockMvc;

    @Mock
    private LeadService leadService;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @BeforeEach
    void setUp() {
        LeadController leadController = new LeadController(leadService);
        mockMvc = MockMvcBuilders.standaloneSetup(leadController)
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
    }

    @Test
    @DisplayName("POST /api/leads returns 201 Created on valid input")
    void createLead_ValidInput_Returns201() throws Exception {
        LeadRequest request = new LeadRequest(1L, 2L, LeadStatus.NEW, "Website", "Interested in demo");

        LeadResponse response = new LeadResponse(
            10L,
            1L,
            "John Contact",
            "john@contact.com",
            "+1-555-1234",
            "Contact Corp",
            2L,
            "Sarah Rep",
            "sarah@maximizecrm.local",
            LeadStatus.NEW,
            "Website",
            "Interested in demo",
            100L,
            "PENDING",
            LocalDateTime.now(),
            LocalDateTime.now()
        );

        when(leadService.createLead(any(LeadRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/leads")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(10))
            .andExpect(jsonPath("$.contactName").value("John Contact"))
            .andExpect(jsonPath("$.assignedUserName").value("Sarah Rep"))
            .andExpect(jsonPath("$.status").value("NEW"))
            .andExpect(jsonPath("$.source").value("Website"))
            .andExpect(jsonPath("$.reminderId").value(100))
            .andExpect(jsonPath("$.reminderStatus").value("PENDING"));
    }

    @Test
    @DisplayName("POST /api/leads returns 400 when required fields are missing")
    void createLead_MissingRequiredFields_Returns400() throws Exception {
        // Missing contactId, assignedUserId, and source
        LeadRequest invalidRequest = new LeadRequest(null, null, LeadStatus.NEW, "", null);

        mockMvc.perform(post("/api/leads")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.errors.contactId").exists())
            .andExpect(jsonPath("$.errors.assignedUserId").exists())
            .andExpect(jsonPath("$.errors.source").exists());
    }

    @Test
    @DisplayName("GET /api/leads returns 200 and list")
    void getLeads_Returns200() throws Exception {
        LeadResponse l1 = new LeadResponse(
            1L, 10L, "Alice", "alice@example.com", null, null,
            20L, "Rep 1", "rep1@maximizecrm.local",
            LeadStatus.NEW, "Website", null, null, null, null, null
        );

        when(leadService.getLeads(null, null, null)).thenReturn(List.of(l1));

        mockMvc.perform(get("/api/leads"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].contactName").value("Alice"))
            .andExpect(jsonPath("$[0].status").value("NEW"));
    }

    @Test
    @DisplayName("GET /api/leads/{id} returns 200 when found")
    void getLeadById_Found_Returns200() throws Exception {
        LeadResponse l1 = new LeadResponse(
            5L, 10L, "Alice", "alice@example.com", null, null,
            20L, "Rep 1", "rep1@maximizecrm.local",
            LeadStatus.QUALIFIED, "Phone", null, null, null, null, null
        );

        when(leadService.getLeadById(5L)).thenReturn(l1);

        mockMvc.perform(get("/api/leads/5"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(5))
            .andExpect(jsonPath("$.status").value("QUALIFIED"));
    }

    @Test
    @DisplayName("GET /api/leads/{id} returns 404 when not found")
    void getLeadById_NotFound_Returns404() throws Exception {
        when(leadService.getLeadById(999L))
            .thenThrow(new ResourceNotFoundException("Lead not found with ID: 999"));

        mockMvc.perform(get("/api/leads/999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("PUT /api/leads/{id} returns 200 on success")
    void updateLead_Success_Returns200() throws Exception {
        LeadRequest request = new LeadRequest(1L, 2L, LeadStatus.CONTACTED, "Phone", "Updated note");
        LeadResponse response = new LeadResponse(
            1L, 1L, "Alice", null, null, null,
            2L, "Rep", null, LeadStatus.CONTACTED, "Phone", "Updated note", null, null, null, null
        );

        when(leadService.updateLead(eq(1L), any(LeadRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/leads/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("CONTACTED"))
            .andExpect(jsonPath("$.notes").value("Updated note"));
    }

    @Test
    @DisplayName("DELETE /api/leads/{id} returns 200 on success")
    void deleteLead_Success_Returns200() throws Exception {
        doNothing().when(leadService).deleteLead(1L);

        mockMvc.perform(delete("/api/leads/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.message").value("Lead deleted successfully"));

        verify(leadService).deleteLead(1L);
    }
}

