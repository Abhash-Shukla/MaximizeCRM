import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { Reminder, ReminderRequest, ReminderStatus } from '../../models/reminder.models';
import { Lead } from '../../models/lead.models';
import { UserSummary } from '../../models/user.models';
import { ReminderService } from '../../services/reminder.service';
import { LeadService } from '../../services/lead.service';
import { UserService } from '../../services/user.service';

export interface ReminderDialogData {
  mode: 'create' | 'edit';
  reminder?: Reminder;
}

@Component({
  selector: 'app-reminder-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './reminder-dialog.component.html',
  styleUrl: './reminder-dialog.component.scss'
})
export class ReminderDialogComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly dialogRef = inject(MatDialogRef<ReminderDialogComponent>);
  private readonly reminderService = inject(ReminderService);
  private readonly leadService = inject(LeadService);
  private readonly userService = inject(UserService);

  protected readonly data: ReminderDialogData = inject(MAT_DIALOG_DATA);

  protected readonly users = signal<UserSummary[]>([]);
  protected readonly leads = signal<Lead[]>([]);
  protected readonly loadingOptions = signal(true);
  protected readonly isSaving = signal(false);
  protected readonly errorMessage = signal<string | null>(null);

  protected readonly statuses: ReminderStatus[] = ['PENDING', 'COMPLETED'];

  // Helper to format LocalDateTime for datetime-local input
  private formatDateTimeLocal(dtStr?: string | null): string {
    if (!dtStr) return '';
    try {
      const d = new Date(dtStr);
      if (isNaN(d.getTime())) return '';
      // Return YYYY-MM-DDTHH:mm
      const pad = (n: number) => n.toString().padStart(2, '0');
      return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
    } catch {
      return '';
    }
  }

  protected readonly form: FormGroup = this.fb.group({
    title: [this.data.reminder?.title ?? '', [Validators.required, Validators.maxLength(200)]],
    dueDate: [this.formatDateTimeLocal(this.data.reminder?.dueDate)],
    leadId: [this.data.reminder?.leadId ?? null],
    assignedUserId: [this.data.reminder?.assignedUserId ?? null, [Validators.required]],
    status: [this.data.reminder?.status ?? 'PENDING', [Validators.required]],
    notes: [this.data.reminder?.notes ?? '']
  });

  get isCreateMode(): boolean {
    return this.data.mode === 'create';
  }

  get title(): string {
    return this.isCreateMode ? 'Schedule New Reminder' : 'Edit Reminder';
  }

  ngOnInit(): void {
    this.loadDropdownOptions();
  }

  private loadDropdownOptions(): void {
    this.loadingOptions.set(true);

    this.userService.getUsers().subscribe({
      next: (userData) => {
        this.users.set(userData);

        // Pre-select first user if create mode and not already selected
        if (this.isCreateMode && !this.form.value.assignedUserId && userData.length > 0) {
          this.form.patchValue({ assignedUserId: userData[0].id });
        }

        // Now load leads
        this.leadService.getLeads().subscribe({
          next: (leadData) => {
            this.leads.set(leadData);
            this.loadingOptions.set(false);
          },
          error: () => {
            this.loadingOptions.set(false);
          }
        });
      },
      error: () => {
        this.loadingOptions.set(false);
      }
    });
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.isSaving.set(true);
    this.errorMessage.set(null);

    const val = this.form.value;
    const request: ReminderRequest = {
      title: val.title.trim(),
      dueDate: val.dueDate ? val.dueDate : null,
      leadId: val.leadId ? Number(val.leadId) : null,
      assignedUserId: Number(val.assignedUserId),
      status: val.status,
      notes: val.notes ? val.notes.trim() : null
    };

    const action$ = this.isCreateMode
      ? this.reminderService.createReminder(request)
      : this.reminderService.updateReminder(this.data.reminder!.id, request);

    action$.subscribe({
      next: (saved) => {
        this.isSaving.set(false);
        this.dialogRef.close(saved);
      },
      error: (err) => {
        this.isSaving.set(false);
        const msg = err.error?.message || (err.error && typeof err.error === 'string' ? err.error : 'Failed to save reminder.');
        this.errorMessage.set(msg);
      }
    });
  }

  onCancel(): void {
    this.dialogRef.close();
  }
}

