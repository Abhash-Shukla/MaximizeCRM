import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatChipsModule } from '@angular/material/chips';
import { MatCardModule } from '@angular/material/card';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { RouterLink } from '@angular/router';

import { Campaign, CampaignStatus, CampaignType } from '../../models/campaign.models';
import { CampaignService } from '../../services/campaign.service';
import { CampaignDialogComponent } from './campaign-dialog.component';

@Component({
  selector: 'app-campaigns',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterLink,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    MatFormFieldModule,
    MatSelectModule,
    MatChipsModule,
    MatCardModule,
    MatDialogModule,
    MatSnackBarModule,
    MatProgressSpinnerModule,
    MatTooltipModule
  ],
  templateUrl: './campaigns.component.html',
  styleUrl: './campaigns.component.scss'
})
export class CampaignsComponent implements OnInit {
  private readonly campaignService = inject(CampaignService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  protected readonly campaigns = signal<Campaign[]>([]);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);

  protected searchTerm = '';
  protected selectedStatus: CampaignStatus | '' = '';

  protected readonly activeCampaignsCount = computed(() => {
    return this.campaigns().filter((c) => c.status === 'ACTIVE').length;
  });

  protected readonly totalBudget = computed(() => {
    return this.campaigns().reduce((sum, c) => sum + (Number(c.budget) || 0), 0);
  });

  protected readonly totalExpectedRevenue = computed(() => {
    return this.campaigns().reduce((sum, c) => sum + (Number(c.expectedRevenue) || 0), 0);
  });

  protected readonly displayedColumns: string[] = [
    'name',
    'type',
    'status',
    'budget',
    'expectedRevenue',
    'assignedUser',
    'dates',
    'actions'
  ];

  ngOnInit(): void {
    this.loadCampaigns();
  }

  loadCampaigns(): void {
    this.loading.set(true);
    this.error.set(null);

    this.campaignService.getCampaigns(this.searchTerm, this.selectedStatus).subscribe({
      next: (data) => {
        this.campaigns.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        const msg = err.error?.message || 'Failed to load campaigns.';
        this.error.set(msg);
        this.loading.set(false);
      }
    });
  }

  onFilterChange(): void {
    this.loadCampaigns();
  }

  clearFilters(): void {
    this.searchTerm = '';
    this.selectedStatus = '';
    this.loadCampaigns();
  }

  openCreateDialog(): void {
    const dialogRef = this.dialog.open(CampaignDialogComponent, {
      width: '580px',
      data: { mode: 'create' }
    });

    dialogRef.afterClosed().subscribe((createdCampaign) => {
      if (createdCampaign) {
        this.snackBar.open(`Campaign "${createdCampaign.name}" created successfully.`, 'Close', {
          duration: 4000,
          horizontalPosition: 'end',
          verticalPosition: 'top'
        });
        this.loadCampaigns();
      }
    });
  }

  openEditDialog(campaign: Campaign): void {
    const dialogRef = this.dialog.open(CampaignDialogComponent, {
      width: '580px',
      data: { mode: 'edit', campaign }
    });

    dialogRef.afterClosed().subscribe((updatedCampaign) => {
      if (updatedCampaign) {
        this.snackBar.open(`Campaign "${updatedCampaign.name}" updated successfully.`, 'Close', {
          duration: 4000,
          horizontalPosition: 'end',
          verticalPosition: 'top'
        });
        this.loadCampaigns();
      }
    });
  }

  deleteCampaign(campaign: Campaign): void {
    const confirmed = window.confirm(`Are you sure you want to delete campaign "${campaign.name}"?`);
    if (!confirmed) {
      return;
    }

    this.campaignService.deleteCampaign(campaign.id).subscribe({
      next: () => {
        this.snackBar.open('Campaign deleted successfully.', 'Close', {
          duration: 3500,
          horizontalPosition: 'end',
          verticalPosition: 'top'
        });
        this.loadCampaigns();
      },
      error: (err) => {
        const msg = err.error?.message || 'Failed to delete campaign.';
        this.snackBar.open(msg, 'Close', {
          duration: 5000,
          horizontalPosition: 'end',
          verticalPosition: 'top'
        });
      }
    });
  }

  getStatusClass(status: CampaignStatus): string {
    switch (status) {
      case 'PLANNING':
        return 'status-planning';
      case 'ACTIVE':
        return 'status-active';
      case 'COMPLETED':
        return 'status-completed';
      case 'CANCELLED':
        return 'status-cancelled';
      default:
        return '';
    }
  }

  getTypeIcon(type: CampaignType): string {
    switch (type) {
      case 'EMAIL':
        return 'mail';
      case 'WEBINAR':
        return 'video_camera_front';
      case 'SOCIAL_MEDIA':
        return 'share';
      case 'EVENT':
        return 'event';
      case 'NEWSLETTER':
        return 'newspaper';
      case 'OTHER':
        return 'campaign';
      default:
        return 'campaign';
    }
  }
}

