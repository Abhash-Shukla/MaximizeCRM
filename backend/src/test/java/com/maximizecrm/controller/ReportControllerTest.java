package com.maximizecrm.controller;

import com.maximizecrm.config.GlobalExceptionHandler;
import com.maximizecrm.dto.ReportSummaryResponse;
import com.maximizecrm.dto.ReportSummaryResponse.StageMetric;
import com.maximizecrm.service.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ReportControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ReportService reportService;

    @BeforeEach
    void setUp() {
        ReportController reportController = new ReportController(reportService);
        mockMvc = MockMvcBuilders.standaloneSetup(reportController)
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
    }

    @Test
    @DisplayName("GET /api/reports/summary should return 200 with summary metrics")
    void getReportSummary_Returns200() throws Exception {
        ReportSummaryResponse summary = new ReportSummaryResponse(
            10,
            4,
            new BigDecimal("50000.00"),
            40.0,
            6,
            new BigDecimal("75000.00"),
            new BigDecimal("12500.00"),
            List.of(new StageMetric("WON", 4, new BigDecimal("50000.00"), 66.7)),
            Collections.emptyList(),
            Collections.emptyList(),
            15,
            2,
            1,
            new BigDecimal("5000.00"),
            new BigDecimal("20000.00"),
            3,
            0
        );

        when(reportService.getReportSummary(any(), any(), any())).thenReturn(summary);

        mockMvc.perform(get("/api/reports/summary"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalLeads").value(10))
            .andExpect(jsonPath("$.dealsWon").value(4))
            .andExpect(jsonPath("$.salesAmount").value(50000.00))
            .andExpect(jsonPath("$.conversionRate").value(40.0));
    }
}

