export interface StageMetric {
  stage: string;
  count: number;
  totalAmount: number;
  percentage: number;
}

export interface SourceMetric {
  source: string;
  count: number;
  percentage: number;
}

export interface StatusMetric {
  status: string;
  count: number;
  percentage: number;
}

export interface ReportSummary {
  totalLeads: number;
  dealsWon: number;
  salesAmount: number;
  conversionRate: number;
  totalDeals: number;
  totalPipelineValue: number;
  averageDealSize: number;
  dealsByStage: StageMetric[];
  leadsBySource: SourceMetric[];
  leadsByStatus: StatusMetric[];
  totalContacts: number;
  totalCampaigns: number;
  activeCampaigns: number;
  totalCampaignBudget: number;
  totalExpectedCampaignRevenue: number;
  pendingReminders: number;
  overdueReminders: number;
}

export interface ReportFilters {
  startDate?: string | null;
  endDate?: string | null;
  assignedUserId?: number | '' | null;
}

