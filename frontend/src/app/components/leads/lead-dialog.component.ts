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
import { MatAutocompleteModule } from '@angular/material/autocomplete';

import { Lead, LeadRequest, LeadStatus } from '../../models/lead.models';
import { Contact } from '../../models/contact.models';
import { UserSummary } from '../../models/user.models';
import { LeadService } from '../../services/lead.service';
import { ContactService } from '../../services/contact.service';
import { UserService } from '../../services/user.service';

export interface LeadDialogData {
  mode: 'create' | 'edit';
  lead?: Lead;
}

@Component({
  selector: 'app-lead-dialog',
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
    MatAutocompleteModule
  ],
  templateUrl: './lead-dialog.component.html',
  styleUrl: './lead-dialog.component.scss'
})
export class LeadDialogComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly dialogRef = inject(MatDialogRef<LeadDialogComponent>);
  private readonly leadService = inject(LeadService);
  private readonly contactService = inject(ContactService);
  private readonly userService = inject(UserService);

  protected readonly data: LeadDialogData = inject(MAT_DIALOG_DATA);

  protected readonly statuses: LeadStatus[] = ['NEW', 'CONTACTED', 'QUALIFIED', 'LOST'];
  protected readonly commonSources: string[] = ['Website', 'Walk-in', 'Referral', 'Phone', 'Email Campaign', 'Trade Show'];

  protected readonly contacts = signal<Contact[]>([]);
  protected readonly users = signal<UserSummary[]>([]);
  protected readonly loadingData = signal(true);
  protected readonly isSaving = signal(false);
  protected readonly errorMessage = signal<string | null>(null);

  protected readonly form: FormGroup = this.fb.group({
    contactId: [this.data.lead?.contactId ?? null, [Validators.required]],
    assignedUserId: [this.data.lead?.assignedUserId ?? null, [Validators.required]],
    status: [this.data.lead?.status ?? 'NEW', [Validators.required]],
    source: [this.data.lead?.source ?? '', [Validators.required, Validators.maxLength(100)]],
    notes: [this.data.lead?.notes ?? '']
  });

  get title(): string {
    return this.data.mode === 'create' ? 'Create New Lead' : 'Edit Lead';
  }

  get submitButtonLabel(): string {
    return this.data.mode === 'create' ? 'Create Lead & Reminder' : 'Save Changes';
  }

  ngOnInit(): void {
    this.loadDropdownData();
  }

  private loadDropdownData(): void {
    this.loadingData.set(true);

    this.contactService.getContacts().subscribe({
      next: (contacts) => {
        this.contacts.set(contacts);
        this.checkDataLoaded();
      },
      error: () => {
        this.errorMessage.set('Failed to load contacts list.');
        this.loadingData.set(false);
      }
    });

    this.userService.getUsers().subscribe({
      next: (users) => {
        this.users.set(users);
        this.checkDataLoaded();
      },
      error: () => {
        this.errorMessage.set('Failed to load users list.');
        this.loadingData.set(false);
      }
    });
  }

  private checkDataLoaded(): void {
    if (this.contacts().length >= 0 && this.users().length >= 0) {
      this.loadingData.set(false);
    }
  }

  setSource(source: string): void {
    this.form.patchValue({ source });
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.isSaving.set(true);
    this.errorMessage.set(null);

    const formValue = this.form.value;
    const request: LeadRequest = {
      contactId: Number(formValue.contactId),
      assignedUserId: Number(formValue.assignedUserId),
      status: formValue.status,
      source: formValue.source?.trim(),
      notes: formValue.notes?.trim() || null
    };

    const action$ = this.data.mode === 'create'
      ? this.leadService.createLead(request)
      : this.leadService.updateLead(this.data.lead!.id, request);

    action$.subscribe({
      next: (result) => {
        this.isSaving.set(false);
        this.dialogRef.close(result);
      },
      error: (err) => {
        this.isSaving.set(false);
        const serverMsg =
          err.error?.message ||
          err.error?.errors?.source ||
          err.error?.errors?.contactId ||
          'Failed to save lead.';
        this.errorMessage.set(serverMsg);
      }
    });
  }

  onCancel(): void {
    this.dialogRef.close();
  }
}

