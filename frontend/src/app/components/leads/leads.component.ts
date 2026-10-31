import { Component, OnInit, inject, signal } from '@angular/core';
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

import { Lead, LeadStatus } from '../../models/lead.models';
import { LeadService } from '../../services/lead.service';
import { LeadDialogComponent } from './lead-dialog.component';

@Component({
  selector: 'app-leads',
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
  templateUrl: './leads.component.html',
  styleUrl: './leads.component.scss'
})
export class LeadsComponent implements OnInit {
  private readonly leadService = inject(LeadService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  protected readonly leads = signal<Lead[]>([]);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);

  protected searchTerm = '';
  protected selectedStatus: LeadStatus | '' = '';

  protected readonly displayedColumns: string[] = [
    'contact',
    'source',
    'status',
    'assignedUser',
    'reminder',
    'createdAt',
    'actions'
  ];

  ngOnInit(): void {
    this.loadLeads();
  }

  loadLeads(): void {
    this.loading.set(true);
    this.error.set(null);

    this.leadService.getLeads(this.searchTerm, this.selectedStatus).subscribe({
      next: (data) => {
        this.leads.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        const msg = err.error?.message || 'Failed to load leads.';
        this.error.set(msg);
        this.loading.set(false);
      }
    });
  }

  onFilterChange(): void {
    this.loadLeads();
  }

  clearFilters(): void {
    this.searchTerm = '';
    this.selectedStatus = '';
    this.loadLeads();
  }

  openCreateDialog(): void {
    const dialogRef = this.dialog.open(LeadDialogComponent, {
      width: '580px',
      data: { mode: 'create' }
    });

    dialogRef.afterClosed().subscribe((result: Lead | undefined) => {
      if (result) {
        this.snackBar.open(
          `Lead for "${result.contactName}" created with automatic follow-up reminder!`,
          'Close',
          {
            duration: 4500,
            horizontalPosition: 'end',
            verticalPosition: 'top'
          }
        );
        this.loadLeads();
      }
    });
  }

  openEditDialog(lead: Lead): void {
    const dialogRef = this.dialog.open(LeadDialogComponent, {
      width: '580px',
      data: { mode: 'edit', lead }
    });

    dialogRef.afterClosed().subscribe((result: Lead | undefined) => {
      if (result) {
        this.snackBar.open(`Lead for "${result.contactName}" updated successfully.`, 'Close', {
          duration: 3500,
          horizontalPosition: 'end',
          verticalPosition: 'top'
        });
        this.loadLeads();
      }
    });
  }

  deleteLead(lead: Lead): void {
    const confirmed = window.confirm(`Are you sure you want to delete lead #${lead.id} for "${lead.contactName}"?`);
    if (!confirmed) {
      return;
    }

    this.leadService.deleteLead(lead.id).subscribe({
      next: () => {
        this.snackBar.open(`Lead #${lead.id} deleted successfully.`, 'Close', {
          duration: 3500,
          horizontalPosition: 'end',
          verticalPosition: 'top'
        });
        this.loadLeads();
      },
      error: (err) => {
        const msg = err.error?.message || 'Failed to delete lead.';
        this.snackBar.open(msg, 'Dismiss', {
          duration: 6000,
          horizontalPosition: 'end',
          verticalPosition: 'top'
        });
      }
    });
  }
}

