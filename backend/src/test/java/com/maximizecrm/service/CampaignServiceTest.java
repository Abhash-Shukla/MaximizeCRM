package com.maximizecrm.service;

import com.maximizecrm.dto.CampaignRequest;
import com.maximizecrm.dto.CampaignResponse;
import com.maximizecrm.entity.Campaign;
import com.maximizecrm.entity.CampaignStatus;
import com.maximizecrm.entity.CampaignType;
import com.maximizecrm.entity.Role;
import com.maximizecrm.entity.User;
import com.maximizecrm.exception.ResourceNotFoundException;
import com.maximizecrm.repository.CampaignRepository;
import com.maximizecrm.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CampaignServiceTest {

    @Mock
    private CampaignRepository campaignRepository;

    @Mock
    private UserRepository userRepository;

    private CampaignService campaignService;
    private User sampleUser;

    @BeforeEach
    void setUp() {
        campaignService = new CampaignService(campaignRepository, userRepository);

        sampleUser = new User("Marketing Lead", "marketing@maximizecrm.local", "hash", Role.ADMIN, true);
        sampleUser.setId(10L);
    }

    @Test
    @DisplayName("createCampaign should successfully create campaign with default PLANNING status")
    void createCampaign_Success() {
        CampaignRequest request = new CampaignRequest(
            "Summer Product Launch",
            CampaignType.EMAIL,
            CampaignStatus.PLANNING,
            LocalDate.of(2026, 6, 1),
            LocalDate.of(2026, 6, 30),
            new BigDecimal("5000.00"),
            new BigDecimal("25000.00"),
            BigDecimal.ZERO,
            10L,
            "Targeting tech buyers"
        );

        Campaign saved = new Campaign(
            "Summer Product Launch",
            CampaignType.EMAIL,
            CampaignStatus.PLANNING,
            LocalDate.of(2026, 6, 1),
            LocalDate.of(2026, 6, 30),
            new BigDecimal("5000.00"),
            new BigDecimal("25000.00"),
            BigDecimal.ZERO,
            sampleUser,
            "Targeting tech buyers"
        );
        saved.setId(1L);
        saved.setCreatedAt(LocalDateTime.now());

        when(userRepository.findById(10L)).thenReturn(Optional.of(sampleUser));
        when(campaignRepository.save(any(Campaign.class))).thenReturn(saved);

        CampaignResponse response = campaignService.createCampaign(request);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("Summer Product Launch", response.name());
        assertEquals(CampaignType.EMAIL, response.type());
        assertEquals(CampaignStatus.PLANNING, response.status());
        assertEquals(new BigDecimal("5000.00"), response.budget());
        assertEquals(10L, response.assignedUserId());
    }

    @Test
    @DisplayName("createCampaign should reject when end date is before start date")
    void createCampaign_EndDateBeforeStartDate_ThrowsIllegalArgumentException() {
        CampaignRequest request = new CampaignRequest(
            "Invalid Dates Campaign",
            CampaignType.WEBINAR,
            CampaignStatus.PLANNING,
            LocalDate.of(2026, 6, 30),
            LocalDate.of(2026, 6, 1),
            new BigDecimal("1000.00"),
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            10L,
            null
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> campaignService.createCampaign(request));

        assertTrue(ex.getMessage().contains("end date cannot be earlier than start date"));
        verify(campaignRepository, never()).save(any());
    }

    @Test
    @DisplayName("createCampaign should reject negative budget")
    void createCampaign_NegativeBudget_ThrowsIllegalArgumentException() {
        CampaignRequest request = new CampaignRequest(
            "Negative Budget",
            CampaignType.SOCIAL_MEDIA,
            CampaignStatus.PLANNING,
            LocalDate.now(),
            LocalDate.now().plusDays(10),
            new BigDecimal("-100.00"),
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            10L,
            null
        );

        assertThrows(IllegalArgumentException.class, () -> campaignService.createCampaign(request));
        verify(campaignRepository, never()).save(any());
    }

    @Test
    @DisplayName("createCampaign should reject inactive user")
    void createCampaign_InactiveUser_ThrowsIllegalArgumentException() {
        sampleUser.setActive(false);

        CampaignRequest request = new CampaignRequest(
            "Campaign with inactive user",
            CampaignType.EVENT,
            CampaignStatus.PLANNING,
            null,
            null,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            10L,
            null
        );

        when(userRepository.findById(10L)).thenReturn(Optional.of(sampleUser));

        assertThrows(IllegalArgumentException.class, () -> campaignService.createCampaign(request));
        verify(campaignRepository, never()).save(any());
    }

    @Test
    @DisplayName("createCampaign should throw ResourceNotFoundException when user does not exist")
    void createCampaign_UserNotFound_ThrowsResourceNotFoundException() {
        CampaignRequest request = new CampaignRequest(
            "Missing User Campaign",
            CampaignType.EMAIL,
            CampaignStatus.PLANNING,
            null,
            null,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            999L,
            null
        );

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> campaignService.createCampaign(request));
        verify(campaignRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateCampaign should successfully update campaign status and details")
    void updateCampaign_Success() {
        Campaign existing = new Campaign(
            "Old Campaign Name",
            CampaignType.EMAIL,
            CampaignStatus.PLANNING,
            LocalDate.now(),
            LocalDate.now().plusDays(5),
            new BigDecimal("1000.00"),
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            sampleUser,
            "Old description"
        );
        existing.setId(2L);

        when(campaignRepository.findById(2L)).thenReturn(Optional.of(existing));
        when(campaignRepository.save(any(Campaign.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CampaignRequest update = new CampaignRequest(
            "Updated Campaign Name",
            CampaignType.WEBINAR,
            CampaignStatus.ACTIVE,
            LocalDate.now(),
            LocalDate.now().plusDays(10),
            new BigDecimal("2500.00"),
            new BigDecimal("10000.00"),
            new BigDecimal("500.00"),
            10L,
            "Updated notes"
        );

        CampaignResponse response = campaignService.updateCampaign(2L, update);

        assertEquals("Updated Campaign Name", response.name());
        assertEquals(CampaignType.WEBINAR, response.type());
        assertEquals(CampaignStatus.ACTIVE, response.status());
        assertEquals(new BigDecimal("2500.00"), response.budget());
        assertEquals(new BigDecimal("10000.00"), response.expectedRevenue());
    }

    @Test
    @DisplayName("deleteCampaign should delete existing campaign")
    void deleteCampaign_Success() {
        Campaign existing = new Campaign(
            "Campaign to Delete",
            CampaignType.OTHER,
            CampaignStatus.CANCELLED,
            null,
            null,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            sampleUser,
            null
        );
        existing.setId(3L);

        when(campaignRepository.findById(3L)).thenReturn(Optional.of(existing));

        campaignService.deleteCampaign(3L);

        verify(campaignRepository, times(1)).delete(existing);
    }
}

