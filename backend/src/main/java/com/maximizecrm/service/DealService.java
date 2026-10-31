package com.maximizecrm.service;

import com.maximizecrm.dto.DealRequest;
import com.maximizecrm.dto.DealResponse;
import com.maximizecrm.dto.LeadResponse;
import com.maximizecrm.entity.*;
import com.maximizecrm.exception.DuplicateResourceException;
import com.maximizecrm.exception.ResourceNotFoundException;
import com.maximizecrm.repository.DealRepository;
import com.maximizecrm.repository.LeadRepository;
import com.maximizecrm.repository.ReminderRepository;
import com.maximizecrm.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class DealService {

    private final DealRepository dealRepository;
    private final LeadRepository leadRepository;
    private final UserRepository userRepository;
    private final ReminderRepository reminderRepository;

    public DealService(
        DealRepository dealRepository,
        LeadRepository leadRepository,
        UserRepository userRepository,
        ReminderRepository reminderRepository
    ) {
        this.dealRepository = dealRepository;
        this.leadRepository = leadRepository;
        this.userRepository = userRepository;
        this.reminderRepository = reminderRepository;
    }

    public DealResponse createDeal(DealRequest request) {
        if (request.amount() == null || request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deal amount must be greater than zero.");
        }

        Lead lead = leadRepository.findById(request.leadId())
            .orElseThrow(() -> new ResourceNotFoundException("Lead not found with ID: " + request.leadId()));

        if (dealRepository.existsByLeadId(request.leadId())) {
            throw new DuplicateResourceException(
                "A deal already exists for lead ID: " + request.leadId() + ". Each lead can have at most one deal."
            );
        }

        User assignedUser = userRepository.findById(request.assignedUserId())
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + request.assignedUserId()));

        if (!assignedUser.isActive()) {
            throw new IllegalArgumentException("Cannot assign deal to an inactive user.");
        }

        // Rule: A newly created deal must start at stage PROSPECT
        if (request.stage() != null && request.stage() != DealStage.PROSPECT) {
            throw new IllegalArgumentException("A newly created deal must start at stage PROSPECT.");
        }

        String name = request.name() != null ? request.name().trim() : "";
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Deal name is required.");
        }

        String notes = request.notes() != null && !request.notes().isBlank() ? request.notes().trim() : null;

        Deal deal = new Deal(name, lead, assignedUser, DealStage.PROSPECT, request.amount(), notes);
        Deal saved = dealRepository.save(deal);

        return DealResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<DealResponse> getDeals(String search, DealStage stage, Long assignedUserId) {
        String cleanSearch = search != null && !search.trim().isEmpty() ? search.trim() : null;
        List<Deal> deals = dealRepository.searchDeals(cleanSearch, stage, assignedUserId);

        return deals.stream()
            .map(DealResponse::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public DealResponse getDealById(Long id) {
        Deal deal = dealRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Deal not found with ID: " + id));

        return DealResponse.fromEntity(deal);
    }

    public DealResponse updateDeal(Long id, DealRequest request) {
        Deal deal = dealRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Deal not found with ID: " + id));

        if (request.amount() != null) {
            if (request.amount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Deal amount must be greater than zero.");
            }
            deal.setAmount(request.amount());
        }

        if (request.name() != null && !request.name().trim().isEmpty()) {
            deal.setName(request.name().trim());
        }

        if (request.leadId() != null && !request.leadId().equals(deal.getLead().getId())) {
            if (dealRepository.existsByLeadIdAndIdNot(request.leadId(), id)) {
                throw new DuplicateResourceException(
                    "A deal already exists for lead ID: " + request.leadId() + ". Each lead can have at most one deal."
                );
            }
            Lead lead = leadRepository.findById(request.leadId())
                .orElseThrow(() -> new ResourceNotFoundException("Lead not found with ID: " + request.leadId()));
            deal.setLead(lead);
        }

        if (request.assignedUserId() != null && !request.assignedUserId().equals(deal.getAssignedUser().getId())) {
            User assignedUser = userRepository.findById(request.assignedUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + request.assignedUserId()));
            if (!assignedUser.isActive()) {
                throw new IllegalArgumentException("Cannot assign deal to an inactive user.");
            }
            deal.setAssignedUser(assignedUser);
        }

        // Rule: Deal stage movement is forward-only. Reject backward transitions.
        if (request.stage() != null && request.stage() != deal.getStage()) {
            DealStage currentStage = deal.getStage();
            DealStage targetStage = request.stage();

            if (!currentStage.canTransitionTo(targetStage)) {
                throw new IllegalArgumentException(
                    "Stage transition from " + currentStage + " to " + targetStage +
                    " is not allowed. Deal stage movement is forward-only."
                );
            }

            deal.setStage(targetStage);

            // Rule: When a deal becomes WON or LOST, set closed_at. For open stages, closed_at must remain empty.
            if (targetStage.isClosed()) {
                if (deal.getClosedAt() == null) {
                    deal.setClosedAt(LocalDateTime.now());
                }
            } else {
                deal.setClosedAt(null);
            }
        }

        if (request.notes() != null) {
            deal.setNotes(request.notes().isBlank() ? null : request.notes().trim());
        }

        Deal updated = dealRepository.save(deal);
        return DealResponse.fromEntity(updated);
    }

    public void deleteDeal(Long id) {
        Deal deal = dealRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Deal not found with ID: " + id));

        dealRepository.delete(deal);
    }

    @Transactional(readOnly = true)
    public List<LeadResponse> getAvailableLeads() {
        List<Long> usedLeadIds = dealRepository.findAllLeadIdsWithDeals();
        Set<Long> usedSet = new HashSet<>(usedLeadIds);

        return leadRepository.findAllByOrderByCreatedAtDesc().stream()
            .filter(lead -> !usedSet.contains(lead.getId()))
            .map(lead -> {
                Reminder reminder = reminderRepository.findFirstByLeadIdOrderByCreatedAtDesc(lead.getId()).orElse(null);
                return LeadResponse.fromEntity(lead, reminder);
            })
            .toList();
    }
}

