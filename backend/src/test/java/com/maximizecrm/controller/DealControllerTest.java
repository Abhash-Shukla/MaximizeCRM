package com.maximizecrm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.maximizecrm.config.GlobalExceptionHandler;
import com.maximizecrm.dto.DealRequest;
import com.maximizecrm.dto.DealResponse;
import com.maximizecrm.entity.DealStage;
import com.maximizecrm.exception.DuplicateResourceException;
import com.maximizecrm.exception.ResourceNotFoundException;
import com.maximizecrm.service.DealService;
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
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class DealControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DealService dealService;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @BeforeEach
    void setUp() {
        DealController dealController = new DealController(dealService);
        mockMvc = MockMvcBuilders.standaloneSetup(dealController)
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
    }

    @Test
    @DisplayName("POST /api/deals returns 201 Created on valid input")
    void createDeal_ValidInput_Returns201() throws Exception {
        DealRequest request = new DealRequest("Enterprise Tier", 10L, 20L, DealStage.PROSPECT, new BigDecimal("35000.00"), "Notes");

        DealResponse response = new DealResponse(
            1L, "Enterprise Tier", 10L, "Website", "QUALIFIED",
            100L, "Acme Contact", "contact@acme.com", "+1-555-0100", "Acme",
            20L, "John Rep", "john@maximizecrm.local",
            DealStage.PROSPECT, new BigDecimal("35000.00"), "Notes",
            null, LocalDateTime.now(), LocalDateTime.now()
        );

        when(dealService.createDeal(any(DealRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/deals")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Enterprise Tier"))
            .andExpect(jsonPath("$.stage").value("PROSPECT"))
            .andExpect(jsonPath("$.amount").value(35000.00))
            .andExpect(jsonPath("$.closedAt").doesNotExist());
    }

    @Test
    @DisplayName("POST /api/deals returns 400 when amount is missing or zero")
    void createDeal_InvalidAmount_Returns400() throws Exception {
        DealRequest request = new DealRequest("Enterprise Tier", 10L, 20L, DealStage.PROSPECT, BigDecimal.ZERO, null);

        mockMvc.perform(post("/api/deals")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());

        verify(dealService, never()).createDeal(any());
    }

    @Test
    @DisplayName("POST /api/deals returns 409 Conflict when duplicate lead is used")
    void createDeal_DuplicateLead_Returns409() throws Exception {
        DealRequest request = new DealRequest("Second Deal", 10L, 20L, DealStage.PROSPECT, new BigDecimal("1000.00"), null);

        when(dealService.createDeal(any(DealRequest.class)))
            .thenThrow(new DuplicateResourceException("A deal already exists for lead ID: 10"));

        mockMvc.perform(post("/api/deals")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.message").value("A deal already exists for lead ID: 10"));
    }

    @Test
    @DisplayName("GET /api/deals returns 200 with list of deals")
    void getDeals_Returns200() throws Exception {
        DealResponse d1 = new DealResponse(
            1L, "Deal 1", 10L, "Website", "QUALIFIED",
            100L, "Acme", "acme@test.com", null, null,
            20L, "John Rep", "john@test.com",
            DealStage.PROSPECT, new BigDecimal("15000.00"), null,
            null, LocalDateTime.now(), LocalDateTime.now()
        );

        when(dealService.getDeals(null, null, null)).thenReturn(List.of(d1));

        mockMvc.perform(get("/api/deals"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].name").value("Deal 1"));
    }

    @Test
    @DisplayName("PUT /api/deals/{id} returns 400 when backward transition attempted")
    void updateDeal_BackwardTransition_Returns400() throws Exception {
        DealRequest request = new DealRequest("Deal 1", 10L, 20L, DealStage.PROSPECT, new BigDecimal("15000.00"), null);

        when(dealService.updateDeal(eq(1L), any(DealRequest.class)))
            .thenThrow(new IllegalArgumentException("Stage transition from NEGOTIATION to PROSPECT is not allowed. Deal stage movement is forward-only."));

        mockMvc.perform(put("/api/deals/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("Stage transition from NEGOTIATION to PROSPECT is not allowed. Deal stage movement is forward-only."));
    }

    @Test
    @DisplayName("DELETE /api/deals/{id} returns 200 on success")
    void deleteDeal_Success_Returns200() throws Exception {
        doNothing().when(dealService).deleteDeal(1L);

        mockMvc.perform(delete("/api/deals/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Deal deleted successfully"));
    }
}

