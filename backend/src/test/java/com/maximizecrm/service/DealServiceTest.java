package com.maximizecrm.service;

import com.maximizecrm.dto.DealRequest;
import com.maximizecrm.dto.DealResponse;
import com.maximizecrm.entity.*;
import com.maximizecrm.exception.DuplicateResourceException;
import com.maximizecrm.exception.ResourceNotFoundException;
import com.maximizecrm.repository.DealRepository;
import com.maximizecrm.repository.LeadRepository;
import com.maximizecrm.repository.ReminderRepository;
import com.maximizecrm.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DealServiceTest {

    @Mock
    private DealRepository dealRepository;

    @Mock
    private LeadRepository leadRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReminderRepository reminderRepository;

    private DealService dealService;

    private Contact sampleContact;
    private User sampleUser;
    private Lead sampleLead;

    @BeforeEach
    void setUp() {
        dealService = new DealService(dealRepository, leadRepository, userRepository, reminderRepository);

        sampleContact = new Contact("Acme Client", "client@acme.com", "+1-555-1111", ContactCategory.VIP, "Acme Corp", null);
        sampleContact.setId(10L);

        sampleUser = new User("John Sales", "john@maximizecrm.local", "hash", Role.SALES_REP, true);
        sampleUser.setId(20L);

        sampleLead = new Lead(sampleContact, sampleUser, LeadStatus.QUALIFIED, "Website", "High interest");
        sampleLead.setId(100L);
    }

    @Test
    @DisplayName("createDeal should create deal starting at stage PROSPECT with closedAt null")
    void createDeal_Success_StartsAtProspect() {
        DealRequest request = new DealRequest("Enterprise Expansion", 100L, 20L, DealStage.PROSPECT, new BigDecimal("45000.00"), "Q4 pipeline");

        Deal savedDeal = new Deal("Enterprise Expansion", sampleLead, sampleUser, DealStage.PROSPECT, new BigDecimal("45000.00"), "Q4 pipeline");
        savedDeal.setId(50L);
        savedDeal.setCreatedAt(LocalDateTime.now());

        when(leadRepository.findById(100L)).thenReturn(Optional.of(sampleLead));
        when(dealRepository.existsByLeadId(100L)).thenReturn(false);
        when(userRepository.findById(20L)).thenReturn(Optional.of(sampleUser));
        when(dealRepository.save(any(Deal.class))).thenReturn(savedDeal);

        DealResponse response = dealService.createDeal(request);

        assertNotNull(response);
        assertEquals(50L, response.id());
        assertEquals("Enterprise Expansion", response.name());
        assertEquals(DealStage.PROSPECT, response.stage());
        assertEquals(new BigDecimal("45000.00"), response.amount());
        assertNull(response.closedAt());
        assertEquals(100L, response.leadId());
        assertEquals(20L, response.assignedUserId());
    }

    @Test
    @DisplayName("createDeal should reject duplicate deal for the same lead")
    void createDeal_DuplicateLead_ThrowsDuplicateResourceException() {
        DealRequest request = new DealRequest("Second Deal", 100L, 20L, DealStage.PROSPECT, new BigDecimal("10000.00"), null);

        when(leadRepository.findById(100L)).thenReturn(Optional.of(sampleLead));
        when(dealRepository.existsByLeadId(100L)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> dealService.createDeal(request));
        verify(dealRepository, never()).save(any());
    }

    @Test
    @DisplayName("createDeal should reject amount less than or equal to zero")
    void createDeal_NonPositiveAmount_ThrowsIllegalArgumentException() {
        DealRequest zeroAmount = new DealRequest("Zero Deal", 100L, 20L, DealStage.PROSPECT, BigDecimal.ZERO, null);
        assertThrows(IllegalArgumentException.class, () -> dealService.createDeal(zeroAmount));

        DealRequest negativeAmount = new DealRequest("Negative Deal", 100L, 20L, DealStage.PROSPECT, new BigDecimal("-50.00"), null);
        assertThrows(IllegalArgumentException.class, () -> dealService.createDeal(negativeAmount));

        verify(dealRepository, never()).save(any());
    }

    @Test
    @DisplayName("createDeal should reject initial stage other than PROSPECT")
    void createDeal_NonProspectInitialStage_ThrowsIllegalArgumentException() {
        DealRequest request = new DealRequest("Direct Won", 100L, 20L, DealStage.WON, new BigDecimal("5000.00"), null);

        when(leadRepository.findById(100L)).thenReturn(Optional.of(sampleLead));
        when(dealRepository.existsByLeadId(100L)).thenReturn(false);
        when(userRepository.findById(20L)).thenReturn(Optional.of(sampleUser));

        assertThrows(IllegalArgumentException.class, () -> dealService.createDeal(request));
        verify(dealRepository, never()).save(any());
    }

    @Test
    @DisplayName("createDeal should reject assignment to inactive user")
    void createDeal_InactiveUser_ThrowsIllegalArgumentException() {
        sampleUser.setActive(false);
        DealRequest request = new DealRequest("Deal", 100L, 20L, DealStage.PROSPECT, new BigDecimal("12000.00"), null);

        when(leadRepository.findById(100L)).thenReturn(Optional.of(sampleLead));
        when(dealRepository.existsByLeadId(100L)).thenReturn(false);
        when(userRepository.findById(20L)).thenReturn(Optional.of(sampleUser));

        assertThrows(IllegalArgumentException.class, () -> dealService.createDeal(request));
        verify(dealRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateDeal should allow forward transition from PROSPECT to NEGOTIATION and keep closedAt null")
    void updateDeal_ProspectToNegotiation_Success() {
        Deal existing = new Deal("Deal", sampleLead, sampleUser, DealStage.PROSPECT, new BigDecimal("15000.00"), null);
        existing.setId(1L);

        when(dealRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(dealRepository.save(any(Deal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DealRequest update = new DealRequest("Deal Updated", 100L, 20L, DealStage.NEGOTIATION, new BigDecimal("18000.00"), "In discussion");
        DealResponse response = dealService.updateDeal(1L, update);

        assertEquals(DealStage.NEGOTIATION, response.stage());
        assertEquals(new BigDecimal("18000.00"), response.amount());
        assertNull(response.closedAt());
    }

    @Test
    @DisplayName("updateDeal should allow forward transition from NEGOTIATION to WON and set closedAt")
    void updateDeal_NegotiationToWon_SetsClosedAt() {
        Deal existing = new Deal("Deal", sampleLead, sampleUser, DealStage.NEGOTIATION, new BigDecimal("25000.00"), null);
        existing.setId(2L);

        when(dealRepository.findById(2L)).thenReturn(Optional.of(existing));
        when(dealRepository.save(any(Deal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DealRequest update = new DealRequest("Deal", 100L, 20L, DealStage.WON, new BigDecimal("25000.00"), "Contract signed");
        DealResponse response = dealService.updateDeal(2L, update);

        assertEquals(DealStage.WON, response.stage());
        assertNotNull(response.closedAt());
    }

    @Test
    @DisplayName("updateDeal should allow forward transition from NEGOTIATION to LOST and set closedAt")
    void updateDeal_NegotiationToLost_SetsClosedAt() {
        Deal existing = new Deal("Deal", sampleLead, sampleUser, DealStage.NEGOTIATION, new BigDecimal("25000.00"), null);
        existing.setId(3L);

        when(dealRepository.findById(3L)).thenReturn(Optional.of(existing));
        when(dealRepository.save(any(Deal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DealRequest update = new DealRequest("Deal", 100L, 20L, DealStage.LOST, new BigDecimal("25000.00"), "Budget cut");
        DealResponse response = dealService.updateDeal(3L, update);

        assertEquals(DealStage.LOST, response.stage());
        assertNotNull(response.closedAt());
    }

    @Test
    @DisplayName("updateDeal should reject backward transition from NEGOTIATION to PROSPECT")
    void updateDeal_NegotiationToProspect_ThrowsIllegalArgumentException() {
        Deal existing = new Deal("Deal", sampleLead, sampleUser, DealStage.NEGOTIATION, new BigDecimal("25000.00"), null);
        existing.setId(4L);

        when(dealRepository.findById(4L)).thenReturn(Optional.of(existing));

        DealRequest backwardRequest = new DealRequest("Deal", 100L, 20L, DealStage.PROSPECT, new BigDecimal("25000.00"), null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> dealService.updateDeal(4L, backwardRequest));

        assertTrue(ex.getMessage().contains("Deal stage movement is forward-only"));
        verify(dealRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateDeal should reject transition from terminal stage WON")
    void updateDeal_FromWon_ThrowsIllegalArgumentException() {
        Deal existing = new Deal("Deal", sampleLead, sampleUser, DealStage.WON, new BigDecimal("25000.00"), null);
        existing.setId(5L);

        when(dealRepository.findById(5L)).thenReturn(Optional.of(existing));

        DealRequest request = new DealRequest("Deal", 100L, 20L, DealStage.NEGOTIATION, new BigDecimal("25000.00"), null);

        assertThrows(IllegalArgumentException.class, () -> dealService.updateDeal(5L, request));
        verify(dealRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateDeal should reject transition from terminal stage LOST")
    void updateDeal_FromLost_ThrowsIllegalArgumentException() {
        Deal existing = new Deal("Deal", sampleLead, sampleUser, DealStage.LOST, new BigDecimal("25000.00"), null);
        existing.setId(6L);

        when(dealRepository.findById(6L)).thenReturn(Optional.of(existing));

        DealRequest request = new DealRequest("Deal", 100L, 20L, DealStage.PROSPECT, new BigDecimal("25000.00"), null);

        assertThrows(IllegalArgumentException.class, () -> dealService.updateDeal(6L, request));
        verify(dealRepository, never()).save(any());
    }

    @Test
    @DisplayName("deleteDeal should delete existing deal")
    void deleteDeal_Success() {
        Deal existing = new Deal("Deal", sampleLead, sampleUser, DealStage.PROSPECT, new BigDecimal("1000.00"), null);
        existing.setId(7L);

        when(dealRepository.findById(7L)).thenReturn(Optional.of(existing));

        dealService.deleteDeal(7L);

        verify(dealRepository, times(1)).delete(existing);
    }
}

