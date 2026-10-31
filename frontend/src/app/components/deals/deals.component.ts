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

import { Deal, DealStage } from '../../models/deal.models';
import { DealService } from '../../services/deal.service';
import { DealDialogComponent } from './deal-dialog.component';

@Component({
  selector: 'app-deals',
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
  templateUrl: './deals.component.html',
  styleUrl: './deals.component.scss'
})
export class DealsComponent implements OnInit {
  private readonly dealService = inject(DealService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  protected readonly deals = signal<Deal[]>([]);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);

  protected searchTerm = '';
  protected selectedStage: DealStage | '' = '';

  protected readonly totalPipelineValue = computed(() => {
    return this.deals()
      .filter((d) => d.stage === 'PROSPECT' || d.stage === 'NEGOTIATION')
      .reduce((sum, d) => sum + (Number(d.amount) || 0), 0);
  });

  protected readonly totalWonValue = computed(() => {
    return this.deals()
      .filter((d) => d.stage === 'WON')
      .reduce((sum, d) => sum + (Number(d.amount) || 0), 0);
  });

  protected readonly displayedColumns: string[] = [
    'name',
    'contact',
    'lead',
    'amount',
    'stage',
    'assignedUser',
    'closedAt',
    'createdAt',
    'actions'
  ];

  ngOnInit(): void {
    this.loadDeals();
  }

  loadDeals(): void {
    this.loading.set(true);
    this.error.set(null);

    this.dealService.getDeals(this.searchTerm, this.selectedStage).subscribe({
      next: (data) => {
        this.deals.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        const msg = err.error?.message || 'Failed to load deals.';
        this.error.set(msg);
        this.loading.set(false);
      }
    });
  }

  onFilterChange(): void {
    this.loadDeals();
  }

  clearFilters(): void {
    this.searchTerm = '';
    this.selectedStage = '';
    this.loadDeals();
  }

  openCreateDialog(): void {
    const dialogRef = this.dialog.open(DealDialogComponent, {
      width: '580px',
      data: { mode: 'create' }
    });

    dialogRef.afterClosed().subscribe((createdDeal) => {
      if (createdDeal) {
        this.snackBar.open(`Deal "${createdDeal.name}" created successfully.`, 'Close', {
          duration: 4000,
          horizontalPosition: 'end',
          verticalPosition: 'top'
        });
        this.loadDeals();
      }
    });
  }

  openEditDialog(deal: Deal): void {
    const dialogRef = this.dialog.open(DealDialogComponent, {
      width: '580px',
      data: { mode: 'edit', deal }
    });

    dialogRef.afterClosed().subscribe((updatedDeal) => {
      if (updatedDeal) {
        this.snackBar.open(`Deal "${updatedDeal.name}" updated successfully.`, 'Close', {
          duration: 4000,
          horizontalPosition: 'end',
          verticalPosition: 'top'
        });
        this.loadDeals();
      }
    });
  }

  deleteDeal(deal: Deal): void {
    const confirmed = window.confirm(`Are you sure you want to delete deal "${deal.name}"?`);
    if (!confirmed) {
      return;
    }

    this.dealService.deleteDeal(deal.id).subscribe({
      next: () => {
        this.snackBar.open('Deal deleted successfully.', 'Close', {
          duration: 3500,
          horizontalPosition: 'end',
          verticalPosition: 'top'
        });
        this.loadDeals();
      },
      error: (err) => {
        const msg = err.error?.message || 'Failed to delete deal.';
        this.snackBar.open(msg, 'Close', {
          duration: 5000,
          horizontalPosition: 'end',
          verticalPosition: 'top'
        });
      }
    });
  }

  getStageClass(stage: DealStage): string {
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

