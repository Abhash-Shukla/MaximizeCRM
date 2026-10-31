package com.maximizecrm.dto;

import java.math.BigDecimal;
import java.util.List;

public record ReportSummaryResponse(
    long totalLeads,
    long dealsWon,
    BigDecimal salesAmount,
    double conversionRate,
    long totalDeals,
    BigDecimal totalPipelineValue,
    BigDecimal averageDealSize,
    List<StageMetric> dealsByStage,
    List<SourceMetric> leadsBySource,
    List<StatusMetric> leadsByStatus,
    long totalContacts,
    long totalCampaigns,
    long activeCampaigns,
    BigDecimal totalCampaignBudget,
    BigDecimal totalExpectedCampaignRevenue,
    long pendingReminders,
    long overdueReminders
) {
    public record StageMetric(
        String stage,
        long count,
        BigDecimal totalAmount,
        double percentage
    ) {}

    public record SourceMetric(
        String source,
        long count,
        double percentage
    ) {}

    public record StatusMetric(
        String status,
        long count,
        double percentage
    ) {}
}

