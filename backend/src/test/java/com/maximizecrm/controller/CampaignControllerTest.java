package com.maximizecrm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.maximizecrm.config.GlobalExceptionHandler;
import com.maximizecrm.dto.CampaignRequest;
import com.maximizecrm.dto.CampaignResponse;
import com.maximizecrm.entity.CampaignStatus;
import com.maximizecrm.entity.CampaignType;
import com.maximizecrm.service.CampaignService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CampaignControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CampaignService campaignService;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @BeforeEach
    void setUp() {
        CampaignController campaignController = new CampaignController(campaignService);
        mockMvc = MockMvcBuilders.standaloneSetup(campaignController)
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
    }

    @Test
    @DisplayName("POST /api/campaigns returns 201 Created on valid input")
    void createCampaign_ValidInput_Returns201() throws Exception {
        CampaignRequest request = new CampaignRequest(
            "Product Launch Webinar",
            CampaignType.WEBINAR,
            CampaignStatus.PLANNING,
            LocalDate.of(2026, 7, 1),
            LocalDate.of(2026, 7, 15),
            new BigDecimal("3000.00"),
            new BigDecimal("15000.00"),
            BigDecimal.ZERO,
            1L,
            "Tech community launch"
        );

        CampaignResponse response = new CampaignResponse(
            1L,
            "Product Launch Webinar",
            CampaignType.WEBINAR,
            CampaignStatus.PLANNING,
            LocalDate.of(2026, 7, 1),
            LocalDate.of(2026, 7, 15),
            new BigDecimal("3000.00"),
            new BigDecimal("15000.00"),
            BigDecimal.ZERO,
            1L,
            "Admin User",
            "admin@maximizecrm.local",
            "Tech community launch",
            LocalDateTime.now(),
            LocalDateTime.now()
        );

        when(campaignService.createCampaign(any(CampaignRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/campaigns")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Product Launch Webinar"))
            .andExpect(jsonPath("$.type").value("WEBINAR"))
            .andExpect(jsonPath("$.status").value("PLANNING"))
            .andExpect(jsonPath("$.budget").value(3000.00));
    }

    @Test
    @DisplayName("POST /api/campaigns returns 400 when name is missing")
    void createCampaign_MissingName_Returns400() throws Exception {
        CampaignRequest request = new CampaignRequest(
            "",
            CampaignType.EMAIL,
            CampaignStatus.PLANNING,
            null,
            null,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            1L,
            null
        );

        mockMvc.perform(post("/api/campaigns")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());

        verify(campaignService, never()).createCampaign(any());
    }

    @Test
    @DisplayName("GET /api/campaigns returns 200 with list of campaigns")
    void getCampaigns_Returns200() throws Exception {
        CampaignResponse c1 = new CampaignResponse(
            1L, "Email Blast", CampaignType.EMAIL, CampaignStatus.ACTIVE,
            null, null, new BigDecimal("500.00"), BigDecimal.ZERO, BigDecimal.ZERO,
            1L, "Admin", "admin@test.com", null, LocalDateTime.now(), LocalDateTime.now()
        );

        when(campaignService.getCampaigns(null, null, null)).thenReturn(List.of(c1));

        mockMvc.perform(get("/api/campaigns"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].name").value("Email Blast"));
    }

    @Test
    @DisplayName("PUT /api/campaigns/{id} returns 200 on successful update")
    void updateCampaign_Returns200() throws Exception {
        CampaignRequest request = new CampaignRequest(
            "Updated Blast",
            CampaignType.EMAIL,
            CampaignStatus.COMPLETED,
            null,
            null,
            new BigDecimal("600.00"),
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            1L,
            null
        );

        CampaignResponse response = new CampaignResponse(
            1L, "Updated Blast", CampaignType.EMAIL, CampaignStatus.COMPLETED,
            null, null, new BigDecimal("600.00"), BigDecimal.ZERO, BigDecimal.ZERO,
            1L, "Admin", "admin@test.com", null, LocalDateTime.now(), LocalDateTime.now()
        );

        when(campaignService.updateCampaign(eq(1L), any(CampaignRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/campaigns/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @DisplayName("DELETE /api/campaigns/{id} returns 200 on success")
    void deleteCampaign_Returns200() throws Exception {
        doNothing().when(campaignService).deleteCampaign(1L);

        mockMvc.perform(delete("/api/campaigns/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Campaign deleted successfully"));
    }
}

