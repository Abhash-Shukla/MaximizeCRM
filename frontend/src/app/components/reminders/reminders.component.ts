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

import { Reminder, ReminderStatus } from '../../models/reminder.models';
import { ReminderService } from '../../services/reminder.service';
import { ReminderDialogComponent } from './reminder-dialog.component';

@Component({
  selector: 'app-reminders',
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
  templateUrl: './reminders.component.html',
  styleUrl: './reminders.component.scss'
})
export class RemindersComponent implements OnInit {
  private readonly reminderService = inject(ReminderService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  protected readonly reminders = signal<Reminder[]>([]);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);

  protected searchTerm = '';
  protected selectedFilter: ReminderStatus | 'OVERDUE' | '' = '';

  protected readonly totalCount = computed(() => this.reminders().length);

  protected readonly pendingCount = computed(() => {
    return this.reminders().filter((r) => r.status === 'PENDING').length;
  });

  protected readonly completedCount = computed(() => {
    return this.reminders().filter((r) => r.status === 'COMPLETED').length;
  });

  protected readonly overdueCount = computed(() => {
    return this.reminders().filter((r) => this.isOverdue(r)).length;
  });

  protected readonly filteredReminders = computed(() => {
    let list = this.reminders();

    if (this.selectedFilter === 'OVERDUE') {
      list = list.filter((r) => this.isOverdue(r));
    } else if (this.selectedFilter) {
      list = list.filter((r) => r.status === this.selectedFilter);
    }

    return list;
  });

  protected readonly displayedColumns: string[] = [
    'title',
    'status',
    'dueDate',
    'lead',
    'assignedUser',
    'createdAt',
    'actions'
  ];

  ngOnInit(): void {
    this.loadReminders();
  }

  loadReminders(): void {
    this.loading.set(true);
    this.error.set(null);

    const apiStatus = (this.selectedFilter === 'PENDING' || this.selectedFilter === 'COMPLETED')
      ? this.selectedFilter
      : '';

    this.reminderService.getReminders(this.searchTerm, apiStatus).subscribe({
      next: (data) => {
        this.reminders.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        const msg = err.error?.message || 'Failed to load reminders.';
        this.error.set(msg);
        this.loading.set(false);
      }
    });
  }

  isOverdue(reminder: Reminder): boolean {
    if (reminder.status === 'COMPLETED' || !reminder.dueDate) {
      return false;
    }
    return reminder.overdue ?? (new Date(reminder.dueDate) < new Date());
  }

  onFilterChange(): void {
    this.loadReminders();
  }

  clearFilters(): void {
    this.searchTerm = '';
    this.selectedFilter = '';
    this.loadReminders();
  }

  openCreateDialog(): void {
    const dialogRef = this.dialog.open(ReminderDialogComponent, {
      width: '560px',
      data: { mode: 'create' }
    });

    dialogRef.afterClosed().subscribe((result: Reminder | undefined) => {
      if (result) {
        this.snackBar.open(`Reminder "${result.title}" scheduled successfully!`, 'Close', {
          duration: 3500
        });
        this.loadReminders();
      }
    });
  }

  openEditDialog(reminder: Reminder): void {
    const dialogRef = this.dialog.open(ReminderDialogComponent, {
      width: '560px',
      data: { mode: 'edit', reminder }
    });

    dialogRef.afterClosed().subscribe((result: Reminder | undefined) => {
      if (result) {
        this.snackBar.open(`Reminder "${result.title}" updated!`, 'Close', {
          duration: 3000
        });
        this.loadReminders();
      }
    });
  }

  markAsCompleted(reminder: Reminder): void {
    this.reminderService.completeReminder(reminder.id).subscribe({
      next: (updated) => {
        this.snackBar.open(`Reminder marked as completed!`, 'Close', {
          duration: 3000
        });
        this.loadReminders();
      },
      error: (err) => {
        const msg = err.error?.message || 'Failed to complete reminder.';
        this.snackBar.open(msg, 'Close', { duration: 4000 });
      }
    });
  }

  deleteReminder(reminder: Reminder): void {
    if (!confirm(`Are you sure you want to delete the reminder "${reminder.title}"?`)) {
      return;
    }

    this.reminderService.deleteReminder(reminder.id).subscribe({
      next: () => {
        this.snackBar.open(`Reminder deleted.`, 'Close', { duration: 3000 });
        this.loadReminders();
      },
      error: (err) => {
        const msg = err.error?.message || 'Failed to delete reminder.';
        this.snackBar.open(msg, 'Close', { duration: 4000 });
      }
    });
  }
}

