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

import { Campaign, CampaignRequest, CampaignStatus, CampaignType } from '../../models/campaign.models';
import { UserSummary } from '../../models/user.models';
import { CampaignService } from '../../services/campaign.service';
import { UserService } from '../../services/user.service';

export interface CampaignDialogData {
  mode: 'create' | 'edit';
  campaign?: Campaign;
}

@Component({
  selector: 'app-campaign-dialog',
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
  templateUrl: './campaign-dialog.component.html',
  styleUrl: './campaign-dialog.component.scss'
})
export class CampaignDialogComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly dialogRef = inject(MatDialogRef<CampaignDialogComponent>);
  private readonly campaignService = inject(CampaignService);
  private readonly userService = inject(UserService);

  protected readonly data: CampaignDialogData = inject(MAT_DIALOG_DATA);

  protected readonly users = signal<UserSummary[]>([]);
  protected readonly loadingUsers = signal(true);
  protected readonly isSaving = signal(false);
  protected readonly errorMessage = signal<string | null>(null);

  protected readonly types: CampaignType[] = ['EMAIL', 'WEBINAR', 'SOCIAL_MEDIA', 'EVENT', 'NEWSLETTER', 'OTHER'];
  protected readonly statuses: CampaignStatus[] = ['PLANNING', 'ACTIVE', 'COMPLETED', 'CANCELLED'];

  protected readonly form: FormGroup = this.fb.group({
    name: [this.data.campaign?.name ?? '', [Validators.required, Validators.maxLength(150)]],
    type: [this.data.campaign?.type ?? 'EMAIL', [Validators.required]],
    status: [this.data.campaign?.status ?? 'PLANNING', [Validators.required]],
    startDate: [this.data.campaign?.startDate ?? ''],
    endDate: [this.data.campaign?.endDate ?? ''],
    budget: [this.data.campaign?.budget ?? 0, [Validators.min(0)]],
    expectedRevenue: [this.data.campaign?.expectedRevenue ?? 0, [Validators.min(0)]],
    actualCost: [this.data.campaign?.actualCost ?? 0, [Validators.min(0)]],
    assignedUserId: [this.data.campaign?.assignedUserId ?? null, [Validators.required]],
    description: [this.data.campaign?.description ?? '']
  });

  get isCreateMode(): boolean {
    return this.data.mode === 'create';
  }

  get title(): string {
    return this.isCreateMode ? 'Create New Campaign' : 'Edit Campaign';
  }

  get submitButtonLabel(): string {
    return this.isCreateMode ? 'Create Campaign' : 'Save Changes';
  }

  ngOnInit(): void {
    this.loadUsers();
  }

  private loadUsers(): void {
    this.loadingUsers.set(true);
    this.userService.getUsers().subscribe({
      next: (users) => {
        this.users.set(users);
        this.loadingUsers.set(false);
      },
      error: () => {
        this.errorMessage.set('Failed to load assignable users.');
        this.loadingUsers.set(false);
      }
    });
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const formValues = this.form.getRawValue();

    if (formValues.startDate && formValues.endDate && formValues.endDate < formValues.startDate) {
      this.errorMessage.set('End date cannot be earlier than start date.');
      return;
    }

    this.isSaving.set(true);
    this.errorMessage.set(null);

    const request: CampaignRequest = {
      name: formValues.name.trim(),
      type: formValues.type,
      status: formValues.status,
      startDate: formValues.startDate ? formValues.startDate : null,
      endDate: formValues.endDate ? formValues.endDate : null,
      budget: formValues.budget !== null && formValues.budget !== '' ? Number(formValues.budget) : 0,
      expectedRevenue: formValues.expectedRevenue !== null && formValues.expectedRevenue !== '' ? Number(formValues.expectedRevenue) : 0,
      actualCost: formValues.actualCost !== null && formValues.actualCost !== '' ? Number(formValues.actualCost) : 0,
      assignedUserId: Number(formValues.assignedUserId),
      description: formValues.description ? formValues.description.trim() : null
    };

    if (this.isCreateMode) {
      this.campaignService.createCampaign(request).subscribe({
        next: (created) => {
          this.isSaving.set(false);
          this.dialogRef.close(created);
        },
        error: (err) => {
          const msg = err.error?.message || 'Failed to create campaign.';
          this.errorMessage.set(msg);
          this.isSaving.set(false);
        }
      });
    } else if (this.data.campaign) {
      this.campaignService.updateCampaign(this.data.campaign.id, request).subscribe({
        next: (updated) => {
          this.isSaving.set(false);
          this.dialogRef.close(updated);
        },
        error: (err) => {
          const msg = err.error?.message || 'Failed to update campaign.';
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

