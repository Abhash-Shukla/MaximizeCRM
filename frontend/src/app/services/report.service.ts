import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ReportFilters, ReportSummary } from '../models/report.models';

@Injectable({
  providedIn: 'root'
})
export class ReportService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/api/reports`;

  getSummary(filters?: ReportFilters): Observable<ReportSummary> {
    let params = new HttpParams();

    if (filters?.startDate) {
      params = params.set('startDate', filters.startDate);
    }
    if (filters?.endDate) {
      params = params.set('endDate', filters.endDate);
    }
    if (filters?.assignedUserId !== undefined && filters?.assignedUserId !== null && filters?.assignedUserId !== '') {
      params = params.set('assignedUserId', filters.assignedUserId.toString());
    }

    return this.http.get<ReportSummary>(`${this.baseUrl}/summary`, { params });
  }

  exportSummaryCsv(data: ReportSummary): void {
    const rows = [
      ['Metric', 'Value'],
      ['Total Leads (FR-7.1)', data.totalLeads.toString()],
      ['Deals Won (FR-7.2)', data.dealsWon.toString()],
      ['Sales Amount (FR-7.3)', `$${data.salesAmount.toFixed(2)}`],
      ['Conversion Rate (FR-7.4)', `${data.conversionRate}%`],
      ['Total Deals', data.totalDeals.toString()],
      ['Total Pipeline Value', `$${data.totalPipelineValue.toFixed(2)}`],
      ['Average Deal Size', `$${data.averageDealSize.toFixed(2)}`],
      ['Total Contacts', data.totalContacts.toString()],
      ['Total Campaigns', data.totalCampaigns.toString()],
      ['Active Campaigns', data.activeCampaigns.toString()],
      ['Total Campaign Budget', `$${data.totalCampaignBudget.toFixed(2)}`],
      ['Expected Campaign Revenue', `$${data.totalExpectedCampaignRevenue.toFixed(2)}`],
      ['Pending Reminders', data.pendingReminders.toString()],
      ['Overdue Reminders', data.overdueReminders.toString()],
      [''],
      ['Stage', 'Count', 'Amount', 'Percentage'],
      ...data.dealsByStage.map((s) => [s.stage, s.count.toString(), `$${s.totalAmount.toFixed(2)}`, `${s.percentage}%`]),
      [''],
      ['Lead Source', 'Count', 'Percentage'],
      ...data.leadsBySource.map((src) => [src.source, src.count.toString(), `${src.percentage}%`])
    ];

    const csvContent = 'data:text/csv;charset=utf-8,' + rows.map((e) => e.join(',')).join('\n');
    const encodedUri = encodeURI(csvContent);
    const link = document.createElement('a');
    link.setAttribute('href', encodedUri);
    link.setAttribute('download', `MaximizeCRM_Report_${new Date().toISOString().slice(0, 10)}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  }
}

