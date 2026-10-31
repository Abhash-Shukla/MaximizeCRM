import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { RouterLink } from '@angular/router';

import { ReportSummary, ReportFilters } from '../../models/report.models';
import { UserSummary } from '../../models/user.models';
import { ReportService } from '../../services/report.service';
import { UserService } from '../../services/user.service';

@Component({
  selector: 'app-reports',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterLink,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatChipsModule,
    MatProgressSpinnerModule,
    MatProgressBarModule,
    MatTooltipModule
  ],
  templateUrl: './reports.component.html',
  styleUrl: './reports.component.scss'
})
export class ReportsComponent implements OnInit {
  private readonly reportService = inject(ReportService);
  private readonly userService = inject(UserService);

  protected readonly summary = signal<ReportSummary | null>(null);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly users = signal<UserSummary[]>([]);

  protected startDate: string = '';
  protected endDate: string = '';
  protected selectedUserId: number | '' = '';
  protected activePreset: 'all' | 'thisMonth' | 'last30Days' | 'thisYear' = 'all';

  ngOnInit(): void {
    this.loadUsers();
    this.loadReport();
  }

  loadUsers(): void {
    this.userService.getUsers().subscribe({
      next: (userData) => this.users.set(userData),
      error: () => {}
    });
  }

  loadReport(): void {
    this.loading.set(true);
    this.error.set(null);

    const filters: ReportFilters = {
      startDate: this.startDate || null,
      endDate: this.endDate || null,
      assignedUserId: this.selectedUserId !== '' ? this.selectedUserId : null
    };

    this.reportService.getSummary(filters).subscribe({
      next: (data) => {
        this.summary.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        const msg = err.error?.message || 'Failed to load report summary.';
        this.error.set(msg);
        this.loading.set(false);
      }
    });
  }

  applyPreset(preset: 'all' | 'thisMonth' | 'last30Days' | 'thisYear'): void {
    this.activePreset = preset;
    const now = new Date();
    const pad = (n: number) => n.toString().padStart(2, '0');
    const toYmd = (d: Date) => `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;

    if (preset === 'all') {
      this.startDate = '';
      this.endDate = '';
    } else if (preset === 'thisMonth') {
      const start = new Date(now.getFullYear(), now.getMonth(), 1);
      this.startDate = toYmd(start);
      this.endDate = toYmd(now);
    } else if (preset === 'last30Days') {
      const start = new Date();
      start.setDate(now.getDate() - 30);
      this.startDate = toYmd(start);
      this.endDate = toYmd(now);
    } else if (preset === 'thisYear') {
      const start = new Date(now.getFullYear(), 0, 1);
      this.startDate = toYmd(start);
      this.endDate = toYmd(now);
    }

    this.loadReport();
  }

  onCustomDateChange(): void {
    this.activePreset = 'all';
    this.loadReport();
  }

  resetFilters(): void {
    this.startDate = '';
    this.endDate = '';
    this.selectedUserId = '';
    this.activePreset = 'all';
    this.loadReport();
  }

  exportCsv(): void {
    const data = this.summary();
    if (data) {
      this.reportService.exportSummaryCsv(data);
    }
  }

  getStageColorClass(stage: string): string {
    switch (stage) {
      case 'PROSPECT':
        return 'stage-prospect';
      case 'NEGOTIATION':
        return 'stage-negotiation';
      case 'WON':
        return 'stage-won';
      case 'LOST':
        return 'stage-lost';
      default:
        return '';
    }
  }
}

