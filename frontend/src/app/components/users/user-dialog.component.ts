import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { CreateUserRequest, UpdateUserRequest, UserResponse, UserRole } from '../../models/user.models';
import { UserService } from '../../services/user.service';

export interface UserDialogData {
  mode: 'create' | 'edit';
  user?: UserResponse;
}

@Component({
  selector: 'app-user-dialog',
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
    MatCheckboxModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './user-dialog.component.html',
  styleUrl: './user-dialog.component.scss'
})
export class UserDialogComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly dialogRef = inject(MatDialogRef<UserDialogComponent>);
  private readonly userService = inject(UserService);

  protected readonly data: UserDialogData = inject(MAT_DIALOG_DATA);

  protected readonly isSaving = signal(false);
  protected readonly errorMessage = signal<string | null>(null);
  protected hidePassword = signal(true);

  protected readonly roles: UserRole[] = ['ADMIN', 'SALES_REP'];

  protected readonly form: FormGroup = this.fb.group({
    fullName: [this.data.user?.fullName ?? '', [Validators.required, Validators.maxLength(100)]],
    email: [this.data.user?.email ?? '', [Validators.required, Validators.email, Validators.maxLength(150)]],
    password: ['', this.data.mode === 'create' ? [Validators.required, Validators.minLength(6)] : [Validators.minLength(6)]],
    role: [this.data.user?.role ?? 'SALES_REP', [Validators.required]],
    active: [this.data.user?.active ?? true]
  });

  get isCreateMode(): boolean {
    return this.data.mode === 'create';
  }

  get title(): string {
    return this.isCreateMode ? 'Create New System User' : 'Edit User Details';
  }

  ngOnInit(): void {}

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.isSaving.set(true);
    this.errorMessage.set(null);

    const val = this.form.value;

    if (this.isCreateMode) {
      const request: CreateUserRequest = {
        fullName: val.fullName.trim(),
        email: val.email.trim().toLowerCase(),
        password: val.password,
        role: val.role,
        active: val.active
      };

      this.userService.createUser(request).subscribe({
        next: (saved) => {
          this.isSaving.set(false);
          this.dialogRef.close(saved);
        },
        error: (err) => {
          this.isSaving.set(false);
          const msg = err.error?.message || 'Failed to create user account.';
          this.errorMessage.set(msg);
        }
      });
    } else {
      const request: UpdateUserRequest = {
        fullName: val.fullName.trim(),
        email: val.email.trim().toLowerCase(),
        password: val.password && val.password.trim() ? val.password.trim() : null,
        role: val.role,
        active: val.active
      };

      this.userService.updateUser(this.data.user!.id, request).subscribe({
        next: (saved) => {
          this.isSaving.set(false);
          this.dialogRef.close(saved);
        },
        error: (err) => {
          this.isSaving.set(false);
          const msg = err.error?.message || 'Failed to update user account.';
          this.errorMessage.set(msg);
        }
      });
    }
  }

  onCancel(): void {
    this.dialogRef.close();
  }
}

