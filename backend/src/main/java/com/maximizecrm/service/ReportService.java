package com.maximizecrm.service;

import com.maximizecrm.dto.ReportSummaryResponse;
import com.maximizecrm.dto.ReportSummaryResponse.SourceMetric;
import com.maximizecrm.dto.ReportSummaryResponse.StageMetric;
import com.maximizecrm.dto.ReportSummaryResponse.StatusMetric;
import com.maximizecrm.entity.*;
import com.maximizecrm.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ReportService {

    private final LeadRepository leadRepository;
    private final DealRepository dealRepository;
    private final ContactRepository contactRepository;
    private final CampaignRepository campaignRepository;
    private final ReminderRepository reminderRepository;

    public ReportService(
        LeadRepository leadRepository,
        DealRepository dealRepository,
        ContactRepository contactRepository,
        CampaignRepository campaignRepository,
        ReminderRepository reminderRepository
    ) {
        this.leadRepository = leadRepository;
        this.dealRepository = dealRepository;
        this.contactRepository = contactRepository;
        this.campaignRepository = campaignRepository;
        this.reminderRepository = reminderRepository;
    }

    public ReportSummaryResponse getReportSummary(LocalDate startDate, LocalDate endDate, Long assignedUserId) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date.");
        }

        // 1. Fetch and filter Leads
        List<Lead> filteredLeads = leadRepository.findAll().stream()
            .filter(l -> isWithinDateRange(l.getCreatedAt(), startDate, endDate))
            .filter(l -> matchesUser(l.getAssignedUser() != null ? l.getAssignedUser().getId() : null, assignedUserId))
            .toList();

        long totalLeads = filteredLeads.size();

        // 2. Fetch and filter Deals
        List<Deal> filteredDeals = dealRepository.findAll().stream()
            .filter(d -> isWithinDateRange(d.getCreatedAt(), startDate, endDate))
            .filter(d -> matchesUser(d.getAssignedUser() != null ? d.getAssignedUser().getId() : null, assignedUserId))
            .toList();

        long totalDeals = filteredDeals.size();

        List<Deal> wonDeals = filteredDeals.stream()
            .filter(d -> d.getStage() == DealStage.WON)
            .toList();

        long dealsWon = wonDeals.size();

        BigDecimal salesAmount = wonDeals.stream()
            .map(Deal::getAmount)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPipelineValue = filteredDeals.stream()
            .map(Deal::getAmount)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Conversion Rate: (Deals Won / Total Leads) * 100. Empty / 0 leads returns 0.0
        double conversionRate = 0.0;
        if (totalLeads > 0) {
            double rate = ((double) dealsWon / totalLeads) * 100.0;
            conversionRate = Math.round(rate * 10.0) / 10.0;
        }

        // Average Deal Size
        BigDecimal averageDealSize = BigDecimal.ZERO;
        if (dealsWon > 0) {
            averageDealSize = salesAmount.divide(BigDecimal.valueOf(dealsWon), 2, RoundingMode.HALF_UP);
        }

        // 3. Deals by Stage (FR-7.5)
        Map<DealStage, List<Deal>> dealsByStageMap = filteredDeals.stream()
            .collect(Collectors.groupingBy(Deal::getStage));

        List<StageMetric> dealsByStage = Arrays.stream(DealStage.values()).map(stage -> {
            List<Deal> stageDeals = dealsByStageMap.getOrDefault(stage, Collections.emptyList());
            long count = stageDeals.size();
            BigDecimal amount = stageDeals.stream()
                .map(Deal::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            double pct = totalDeals > 0 ? Math.round(((double) count / totalDeals * 100.0) * 10.0) / 10.0 : 0.0;
            return new StageMetric(stage.name(), count, amount, pct);
        }).toList();

        // 4. Leads by Source (FR-7.6)
        Map<String, Long> sourceCounts = filteredLeads.stream()
            .collect(Collectors.groupingBy(
                l -> (l.getSource() != null && !l.getSource().trim().isEmpty()) ? l.getSource().trim() : "Unknown",
                Collectors.counting()
            ));

        List<SourceMetric> leadsBySource = sourceCounts.entrySet().stream()
            .map(entry -> {
                double pct = totalLeads > 0 ? Math.round(((double) entry.getValue() / totalLeads * 100.0) * 10.0) / 10.0 : 0.0;
                return new SourceMetric(entry.getKey(), entry.getValue(), pct);
            })
            .sorted((a, b) -> Long.compare(b.count(), a.count()))
            .toList();

        // 5. Leads by Status
        Map<LeadStatus, Long> statusCounts = filteredLeads.stream()
            .collect(Collectors.groupingBy(Lead::getStatus, Collectors.counting()));

        List<StatusMetric> leadsByStatus = Arrays.stream(LeadStatus.values()).map(status -> {
            long count = statusCounts.getOrDefault(status, 0L);
            double pct = totalLeads > 0 ? Math.round(((double) count / totalLeads * 100.0) * 10.0) / 10.0 : 0.0;
            return new StatusMetric(status.name(), count, pct);
        }).toList();

        // 6. Contacts metrics
        long totalContacts = contactRepository.findAll().stream()
            .filter(c -> isWithinDateRange(c.getCreatedAt(), startDate, endDate))
            .count();

        // 7. Campaigns metrics
        List<Campaign> filteredCampaigns = campaignRepository.findAll().stream()
            .filter(c -> isWithinDateRange(c.getCreatedAt(), startDate, endDate))
            .filter(c -> matchesUser(c.getAssignedUser() != null ? c.getAssignedUser().getId() : null, assignedUserId))
            .toList();

        long totalCampaigns = filteredCampaigns.size();
        long activeCampaigns = filteredCampaigns.stream()
            .filter(c -> c.getStatus() == CampaignStatus.ACTIVE)
            .count();

        BigDecimal totalCampaignBudget = filteredCampaigns.stream()
            .map(Campaign::getBudget)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpectedCampaignRevenue = filteredCampaigns.stream()
            .map(Campaign::getExpectedRevenue)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 8. Reminders metrics
        LocalDateTime now = LocalDateTime.now();
        List<Reminder> filteredReminders = reminderRepository.findAll().stream()
            .filter(r -> isWithinDateRange(r.getCreatedAt(), startDate, endDate))
            .filter(r -> matchesUser(r.getAssignedUser() != null ? r.getAssignedUser().getId() : null, assignedUserId))
            .toList();

        long pendingReminders = filteredReminders.stream()
            .filter(r -> r.getStatus() == ReminderStatus.PENDING)
            .count();

        long overdueReminders = filteredReminders.stream()
            .filter(r -> r.getStatus() == ReminderStatus.PENDING && r.getDueDate() != null && r.getDueDate().isBefore(now))
            .count();

        return new ReportSummaryResponse(
            totalLeads,
            dealsWon,
            salesAmount,
            conversionRate,
            totalDeals,
            totalPipelineValue,
            averageDealSize,
            dealsByStage,
            leadsBySource,
            leadsByStatus,
            totalContacts,
            totalCampaigns,
            activeCampaigns,
            totalCampaignBudget,
            totalExpectedCampaignRevenue,
            pendingReminders,
            overdueReminders
        );
    }

    private boolean isWithinDateRange(LocalDateTime dateTime, LocalDate startDate, LocalDate endDate) {
        if (startDate == null && endDate == null) {
            return true;
        }
        if (dateTime == null) {
            return false;
        }
        LocalDate date = dateTime.toLocalDate();
        if (startDate != null && date.isBefore(startDate)) {
            return false;
        }
        if (endDate != null && date.isAfter(endDate)) {
            return false;
        }
        return true;
    }

    private boolean matchesUser(Long recordUserId, Long assignedUserId) {
        if (assignedUserId == null) {
            return true;
        }
        return recordUserId != null && recordUserId.equals(assignedUserId);
    }
}

