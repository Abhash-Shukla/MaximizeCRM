package com.maximizecrm.service;

import com.maximizecrm.dto.ReportSummaryResponse;
import com.maximizecrm.entity.*;
import com.maximizecrm.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private LeadRepository leadRepository;

    @Mock
    private DealRepository dealRepository;

    @Mock
    private ContactRepository contactRepository;

    @Mock
    private CampaignRepository campaignRepository;

    @Mock
    private ReminderRepository reminderRepository;

    private ReportService reportService;

    private User sampleUser;
    private Contact sampleContact;

    @BeforeEach
    void setUp() {
        reportService = new ReportService(
            leadRepository,
            dealRepository,
            contactRepository,
            campaignRepository,
            reminderRepository
        );

        sampleUser = new User("Jane Rep", "jane@maximizecrm.local", "hash", Role.SALES_REP, true);
        sampleUser.setId(10L);

        sampleContact = new Contact("Acme Client", "client@acme.com", "+1-555-1234", ContactCategory.VIP, "Acme Corp", null);
        sampleContact.setId(20L);
    }

    @Test
    @DisplayName("getReportSummary with empty database should return zeroed metrics (FR-7.1 to 7.4)")
    void getReportSummary_EmptyDatabase_ReturnsZeros() {
        when(leadRepository.findAll()).thenReturn(Collections.emptyList());
        when(dealRepository.findAll()).thenReturn(Collections.emptyList());
        when(contactRepository.findAll()).thenReturn(Collections.emptyList());
        when(campaignRepository.findAll()).thenReturn(Collections.emptyList());
        when(reminderRepository.findAll()).thenReturn(Collections.emptyList());

        ReportSummaryResponse response = reportService.getReportSummary(null, null, null);

        assertNotNull(response);
        assertEquals(0, response.totalLeads());
        assertEquals(0, response.dealsWon());
        assertEquals(BigDecimal.ZERO, response.salesAmount());
        assertEquals(0.0, response.conversionRate());
        assertEquals(0, response.totalDeals());
        assertEquals(BigDecimal.ZERO, response.totalPipelineValue());
        assertEquals(BigDecimal.ZERO, response.averageDealSize());
        assertEquals(4, response.dealsByStage().size()); // 4 standard stages with 0 count
        assertEquals(4, response.leadsByStatus().size()); // 4 standard statuses with 0 count
    }

    @Test
    @DisplayName("getReportSummary should accurately calculate metrics, conversion rate, and breakdowns")
    void getReportSummary_WithData_ComputesCorrectMetrics() {
        Lead lead1 = new Lead(sampleContact, sampleUser, LeadStatus.QUALIFIED, "Website", "Inbound lead");
        lead1.setId(100L);
        lead1.setCreatedAt(LocalDateTime.of(2026, 5, 1, 10, 0));

        Lead lead2 = new Lead(sampleContact, sampleUser, LeadStatus.NEW, "Referral", "Referral lead");
        lead2.setId(101L);
        lead2.setCreatedAt(LocalDateTime.of(2026, 5, 2, 11, 0));

        when(leadRepository.findAll()).thenReturn(List.of(lead1, lead2));

        Deal dealWon = new Deal("Enterprise Contract", lead1, sampleUser, DealStage.WON, new BigDecimal("25000.00"), null);
        dealWon.setId(200L);
        dealWon.setCreatedAt(LocalDateTime.of(2026, 5, 5, 12, 0));

        Deal dealNegotiation = new Deal("Pilot Program", lead2, sampleUser, DealStage.NEGOTIATION, new BigDecimal("10000.00"), null);
        dealNegotiation.setId(201L);
        dealNegotiation.setCreatedAt(LocalDateTime.of(2026, 5, 6, 14, 0));

        when(dealRepository.findAll()).thenReturn(List.of(dealWon, dealNegotiation));
        when(contactRepository.findAll()).thenReturn(List.of(sampleContact));
        when(campaignRepository.findAll()).thenReturn(Collections.emptyList());
        when(reminderRepository.findAll()).thenReturn(Collections.emptyList());

        ReportSummaryResponse response = reportService.getReportSummary(null, null, null);

        assertEquals(2, response.totalLeads());
        assertEquals(1, response.dealsWon());
        assertEquals(new BigDecimal("25000.00"), response.salesAmount());
        assertEquals(2, response.totalDeals());
        assertEquals(new BigDecimal("35000.00"), response.totalPipelineValue());
        // Conversion Rate = (1 / 2) * 100 = 50.0%
        assertEquals(50.0, response.conversionRate());
        assertEquals(new BigDecimal("25000.00"), response.averageDealSize());

        // Check deals by stage
        assertEquals(4, response.dealsByStage().size());
        var wonStage = response.dealsByStage().stream().filter(s -> s.stage().equals("WON")).findFirst().orElseThrow();
        assertEquals(1, wonStage.count());
        assertEquals(new BigDecimal("25000.00"), wonStage.totalAmount());
        assertEquals(50.0, wonStage.percentage());

        // Check leads by source
        assertEquals(2, response.leadsBySource().size());
    }

    @Test
    @DisplayName("getReportSummary with date filters should include only records in range")
    void getReportSummary_DateFiltering() {
        Lead leadMay = new Lead(sampleContact, sampleUser, LeadStatus.NEW, "Website", null);
        leadMay.setCreatedAt(LocalDateTime.of(2026, 5, 15, 10, 0));

        Lead leadJune = new Lead(sampleContact, sampleUser, LeadStatus.NEW, "Referral", null);
        leadJune.setCreatedAt(LocalDateTime.of(2026, 6, 15, 10, 0));

        when(leadRepository.findAll()).thenReturn(List.of(leadMay, leadJune));
        when(dealRepository.findAll()).thenReturn(Collections.emptyList());
        when(contactRepository.findAll()).thenReturn(Collections.emptyList());
        when(campaignRepository.findAll()).thenReturn(Collections.emptyList());
        when(reminderRepository.findAll()).thenReturn(Collections.emptyList());

        LocalDate start = LocalDate.of(2026, 5, 1);
        LocalDate end = LocalDate.of(2026, 5, 31);

        ReportSummaryResponse response = reportService.getReportSummary(start, end, null);

        assertEquals(1, response.totalLeads());
        assertEquals("Website", response.leadsBySource().get(0).source());
    }

    @Test
    @DisplayName("getReportSummary should throw when startDate is after endDate")
    void getReportSummary_InvalidDateRange_ThrowsException() {
        LocalDate start = LocalDate.of(2026, 6, 1);
        LocalDate end = LocalDate.of(2026, 5, 1);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> reportService.getReportSummary(start, end, null));
        assertTrue(ex.getMessage().contains("Start date cannot be after end date"));
    }
}

