package com.maximizecrm.service;

import com.maximizecrm.dto.CampaignRequest;
import com.maximizecrm.dto.CampaignResponse;
import com.maximizecrm.entity.Campaign;
import com.maximizecrm.entity.CampaignStatus;
import com.maximizecrm.entity.CampaignType;
import com.maximizecrm.entity.User;
import com.maximizecrm.exception.ResourceNotFoundException;
import com.maximizecrm.repository.CampaignRepository;
import com.maximizecrm.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class CampaignService {

    private final CampaignRepository campaignRepository;
    private final UserRepository userRepository;

    public CampaignService(CampaignRepository campaignRepository, UserRepository userRepository) {
        this.campaignRepository = campaignRepository;
        this.userRepository = userRepository;
    }

    public CampaignResponse createCampaign(CampaignRequest request) {
        validateDates(request);
        validateAmounts(request);

        User assignedUser = userRepository.findById(request.assignedUserId())
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + request.assignedUserId()));

        if (!assignedUser.isActive()) {
            throw new IllegalArgumentException("Cannot assign campaign to an inactive user.");
        }

        String name = request.name() != null ? request.name().trim() : "";
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Campaign name is required.");
        }

        CampaignType type = request.type() != null ? request.type() : CampaignType.EMAIL;
        CampaignStatus status = request.status() != null ? request.status() : CampaignStatus.PLANNING;
        BigDecimal budget = request.budget() != null ? request.budget() : BigDecimal.ZERO;
        BigDecimal expectedRevenue = request.expectedRevenue() != null ? request.expectedRevenue() : BigDecimal.ZERO;
        BigDecimal actualCost = request.actualCost() != null ? request.actualCost() : BigDecimal.ZERO;
        String description = request.description() != null && !request.description().isBlank()
            ? request.description().trim() : null;

        Campaign campaign = new Campaign(
            name,
            type,
            status,
            request.startDate(),
            request.endDate(),
            budget,
            expectedRevenue,
            actualCost,
            assignedUser,
            description
        );

        Campaign saved = campaignRepository.save(campaign);
        return CampaignResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<CampaignResponse> getCampaigns(String search, CampaignStatus status, Long assignedUserId) {
        String cleanSearch = search != null && !search.trim().isEmpty() ? search.trim() : null;
        List<Campaign> campaigns = campaignRepository.searchCampaigns(cleanSearch, status, assignedUserId);

        return campaigns.stream()
            .map(CampaignResponse::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public CampaignResponse getCampaignById(Long id) {
        Campaign campaign = campaignRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Campaign not found with ID: " + id));

        return CampaignResponse.fromEntity(campaign);
    }

    public CampaignResponse updateCampaign(Long id, CampaignRequest request) {
        Campaign campaign = campaignRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Campaign not found with ID: " + id));

        validateDates(request);
        validateAmounts(request);

        if (request.name() != null && !request.name().trim().isEmpty()) {
            campaign.setName(request.name().trim());
        }

        if (request.type() != null) {
            campaign.setType(request.type());
        }

        if (request.status() != null) {
            campaign.setStatus(request.status());
        }

        campaign.setStartDate(request.startDate());
        campaign.setEndDate(request.endDate());

        if (request.budget() != null) {
            campaign.setBudget(request.budget());
        }
        if (request.expectedRevenue() != null) {
            campaign.setExpectedRevenue(request.expectedRevenue());
        }
        if (request.actualCost() != null) {
            campaign.setActualCost(request.actualCost());
        }

        if (request.assignedUserId() != null && !request.assignedUserId().equals(campaign.getAssignedUser().getId())) {
            User assignedUser = userRepository.findById(request.assignedUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + request.assignedUserId()));
            if (!assignedUser.isActive()) {
                throw new IllegalArgumentException("Cannot assign campaign to an inactive user.");
            }
            campaign.setAssignedUser(assignedUser);
        }

        campaign.setDescription(
            request.description() != null && !request.description().isBlank() ? request.description().trim() : null
        );

        Campaign updated = campaignRepository.save(campaign);
        return CampaignResponse.fromEntity(updated);
    }

    public void deleteCampaign(Long id) {
        Campaign campaign = campaignRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Campaign not found with ID: " + id));

        campaignRepository.delete(campaign);
    }

    private void validateDates(CampaignRequest request) {
        if (request.startDate() != null && request.endDate() != null) {
            if (request.endDate().isBefore(request.startDate())) {
                throw new IllegalArgumentException("Campaign end date cannot be earlier than start date.");
            }
        }
    }

    private void validateAmounts(CampaignRequest request) {
        if (request.budget() != null && request.budget().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Campaign budget must be greater than or equal to zero.");
        }
        if (request.expectedRevenue() != null && request.expectedRevenue().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Expected revenue must be greater than or equal to zero.");
        }
        if (request.actualCost() != null && request.actualCost().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Actual cost must be greater than or equal to zero.");
        }
    }
}

