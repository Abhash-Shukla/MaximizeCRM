import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { Contact, ContactCategory, ContactRequest } from '../../models/contact.models';
import { ContactService } from '../../services/contact.service';

export interface ContactDialogData {
  mode: 'create' | 'edit';
  contact?: Contact;
}

@Component({
  selector: 'app-contact-dialog',
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
  templateUrl: './contact-dialog.component.html',
  styleUrl: './contact-dialog.component.scss'
})
export class ContactDialogComponent {
  private readonly fb = inject(FormBuilder);
  private readonly dialogRef = inject(MatDialogRef<ContactDialogComponent>);
  private readonly contactService = inject(ContactService);
  protected readonly data: ContactDialogData = inject(MAT_DIALOG_DATA);

  protected readonly categories: ContactCategory[] = ['NEW', 'REGULAR', 'VIP'];
  protected readonly isSaving = signal(false);
  protected readonly errorMessage = signal<string | null>(null);

  protected readonly form: FormGroup = this.fb.group({
    name: [this.data.contact?.name ?? '', [Validators.required, Validators.maxLength(100)]],
    email: [this.data.contact?.email ?? '', [Validators.email, Validators.maxLength(150)]],
    phone: [this.data.contact?.phone ?? '', [Validators.maxLength(30)]],
    category: [this.data.contact?.category ?? 'NEW', [Validators.required]],
    company: [this.data.contact?.company ?? '', [Validators.maxLength(100)]],
    notes: [this.data.contact?.notes ?? '']
  });

  get title(): string {
    return this.data.mode === 'create' ? 'Add New Contact' : 'Edit Contact';
  }

  get submitButtonLabel(): string {
    return this.data.mode === 'create' ? 'Create Contact' : 'Save Changes';
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.isSaving.set(true);
    this.errorMessage.set(null);

    const formValue = this.form.value;
    const request: ContactRequest = {
      name: formValue.name?.trim(),
      email: formValue.email?.trim() || null,
      phone: formValue.phone?.trim() || null,
      category: formValue.category,
      company: formValue.company?.trim() || null,
      notes: formValue.notes?.trim() || null
    };

    const action$ = this.data.mode === 'create'
      ? this.contactService.createContact(request)
      : this.contactService.updateContact(this.data.contact!.id, request);

    action$.subscribe({
      next: (result) => {
        this.isSaving.set(false);
        this.dialogRef.close(result);
      },
      error: (err) => {
        this.isSaving.set(false);
        const serverMsg = err.error?.message || err.error?.errors?.name || 'Failed to save contact.';
        this.errorMessage.set(serverMsg);
      }
    });
  }

  onCancel(): void {
    this.dialogRef.close();
  }
}

