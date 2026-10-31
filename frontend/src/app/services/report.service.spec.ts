import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ReportService } from './report.service';
import { ReportSummary } from '../models/report.models';

describe('ReportService', () => {
  let service: ReportService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.resetTestingModule();

    TestBed.configureTestingModule({
      providers: [
        ReportService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });

    service = TestBed.inject(ReportService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should fetch report summary without filters', () => {
    const mockSummary: ReportSummary = {
      totalLeads: 12,
      dealsWon: 3,
      salesAmount: 45000,
      conversionRate: 25.0,
      totalDeals: 5,
      totalPipelineValue: 70000,
      averageDealSize: 15000,
      dealsByStage: [
        { stage: 'PROSPECT', count: 1, totalAmount: 10000, percentage: 20 },
        { stage: 'NEGOTIATION', count: 1, totalAmount: 15000, percentage: 20 },
        { stage: 'WON', count: 3, totalAmount: 45000, percentage: 60 },
        { stage: 'LOST', count: 0, totalAmount: 0, percentage: 0 }
      ],
      leadsBySource: [
        { source: 'Website', count: 8, percentage: 66.7 },
        { source: 'Referral', count: 4, percentage: 33.3 }
      ],
      leadsByStatus: [],
      totalContacts: 20,
      totalCampaigns: 2,
      activeCampaigns: 1,
      totalCampaignBudget: 10000,
      totalExpectedCampaignRevenue: 50000,
      pendingReminders: 2,
      overdueReminders: 0
    };

    service.getSummary().subscribe((summary) => {
      expect(summary.totalLeads).toBe(12);
      expect(summary.dealsWon).toBe(3);
      expect(summary.salesAmount).toBe(45000);
      expect(summary.conversionRate).toBe(25.0);
    });

    const req = httpMock.expectOne('/api/reports/summary');
    expect(req.request.method).toBe('GET');
    req.flush(mockSummary);
  });

  it('should fetch report summary with date range and user filters', () => {
    service.getSummary({
      startDate: '2026-01-01',
      endDate: '2026-06-30',
      assignedUserId: 5
    }).subscribe();

    const req = httpMock.expectOne('/api/reports/summary?startDate=2026-01-01&endDate=2026-06-30&assignedUserId=5');
    expect(req.request.method).toBe('GET');
    req.flush({ totalLeads: 0, dealsWon: 0, salesAmount: 0, conversionRate: 0.0 });
  });
});

