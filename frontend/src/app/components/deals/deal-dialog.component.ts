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
import { MatChipsModule } from '@angular/material/chips';

import { Deal, DealRequest, DealStage } from '../../models/deal.models';
import { Lead } from '../../models/lead.models';
import { UserSummary } from '../../models/user.models';
import { DealService } from '../../services/deal.service';
import { UserService } from '../../services/user.service';

export interface DealDialogData {
  mode: 'create' | 'edit';
  deal?: Deal;
}

@Component({
  selector: 'app-deal-dialog',
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
    MatProgressSpinnerModule,
    MatChipsModule
  ],
  templateUrl: './deal-dialog.component.html',
  styleUrl: './deal-dialog.component.scss'
})
export class DealDialogComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly dialogRef = inject(MatDialogRef<DealDialogComponent>);
  private readonly dealService = inject(DealService);
  private readonly userService = inject(UserService);

  protected readonly data: DealDialogData = inject(MAT_DIALOG_DATA);

  protected readonly availableLeads = signal<Lead[]>([]);
  protected readonly users = signal<UserSummary[]>([]);
  protected readonly loadingData = signal(true);
  protected readonly isSaving = signal(false);
  protected readonly errorMessage = signal<string | null>(null);

  protected allowedStages: DealStage[] = [];

  protected readonly form: FormGroup = this.fb.group({
    leadId: [this.data.deal?.leadId ?? null, [Validators.required]],
    name: [this.data.deal?.name ?? '', [Validators.required, Validators.maxLength(150)]],
    assignedUserId: [this.data.deal?.assignedUserId ?? null, [Validators.required]],
    stage: [{ value: this.data.deal?.stage ?? 'PROSPECT', disabled: this.data.mode === 'create' }, [Validators.required]],
    amount: [this.data.deal?.amount ?? '', [Validators.required, Validators.min(0.01)]],
    notes: [this.data.deal?.notes ?? '']
  });

  get isCreateMode(): boolean {
    return this.data.mode === 'create';
  }

  get title(): string {
    return this.isCreateMode ? 'Create New Deal' : 'Edit Deal';
  }

  get submitButtonLabel(): string {
    return this.isCreateMode ? 'Create Deal' : 'Save Changes';
  }

  ngOnInit(): void {
    if (this.isCreateMode) {
      this.allowedStages = ['PROSPECT'];
    } else if (this.data.deal) {
      this.allowedStages = this.computeAllowedStages(this.data.deal.stage);
    }
    this.loadDropdownData();
  }

  private computeAllowedStages(currentStage: DealStage): DealStage[] {
    switch (currentStage) {
      case 'PROSPECT':
        return ['PROSPECT', 'NEGOTIATION', 'WON', 'LOST'];
      case 'NEGOTIATION':
        return ['NEGOTIATION', 'WON', 'LOST'];
      case 'WON':
        return ['WON'];
      case 'LOST':
        return ['LOST'];
      default:
        return ['PROSPECT'];
    }
  }

  private loadDropdownData(): void {
    this.loadingData.set(true);

    if (this.isCreateMode) {
      this.dealService.getAvailableLeads().subscribe({
        next: (leads) => {
          this.availableLeads.set(leads);
          this.loadUsers();
        },
        error: () => {
          this.errorMessage.set('Failed to load available leads.');
          this.loadingData.set(false);
        }
      });
    } else {
      this.loadUsers();
    }
  }

  private loadUsers(): void {
    this.userService.getUsers().subscribe({
      next: (users) => {
        this.users.set(users);
        this.loadingData.set(false);
      },
      error: () => {
        this.errorMessage.set('Failed to load assignable users.');
        this.loadingData.set(false);
      }
    });
  }

  onLeadSelected(leadId: number): void {
    const selected = this.availableLeads().find((l) => l.id === leadId);
    if (selected) {
      if (!this.form.get('name')?.value || this.form.get('name')?.pristine) {
        this.form.patchValue({
          name: `${selected.contactName} - ${selected.source} Deal`
        });
      }
      if (!this.form.get('assignedUserId')?.value) {
        this.form.patchValue({
          assignedUserId: selected.assignedUserId
        });
      }
    }
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.isSaving.set(true);
    this.errorMessage.set(null);

    const formValues = this.form.getRawValue();
    const request: DealRequest = {
      name: formValues.name.trim(),
      leadId: Number(formValues.leadId),
      assignedUserId: Number(formValues.assignedUserId),
      stage: this.isCreateMode ? 'PROSPECT' : formValues.stage,
      amount: Number(formValues.amount),
      notes: formValues.notes ? formValues.notes.trim() : null
    };

    if (this.isCreateMode) {
      this.dealService.createDeal(request).subscribe({
        next: (created) => {
          this.isSaving.set(false);
          this.dialogRef.close(created);
        },
        error: (err) => {
          const msg = err.error?.message || 'Failed to create deal. Please verify input.';
          this.errorMessage.set(msg);
          this.isSaving.set(false);
        }
      });
    } else if (this.data.deal) {
      this.dealService.updateDeal(this.data.deal.id, request).subscribe({
        next: (updated) => {
          this.isSaving.set(false);
          this.dialogRef.close(updated);
        },
        error: (err) => {
          const msg = err.error?.message || 'Failed to update deal.';
          this.errorMessage.set(msg);
          this.isSaving.set(false);
        }
      });
    }
  }

  onCancel(): void {
    this.dialogRef.close(null);
  }
}

