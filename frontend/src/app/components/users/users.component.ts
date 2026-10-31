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

import { UserResponse, UserRole } from '../../models/user.models';
import { UserService } from '../../services/user.service';
import { AuthService } from '../../services/auth.service';
import { UserDialogComponent } from './user-dialog.component';

@Component({
  selector: 'app-users',
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
  templateUrl: './users.component.html',
  styleUrl: './users.component.scss'
})
export class UsersComponent implements OnInit {
  private readonly userService = inject(UserService);
  private readonly authService = inject(AuthService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  protected readonly users = signal<UserResponse[]>([]);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);

  protected searchTerm = '';
  protected selectedRole: UserRole | '' = '';
  protected selectedActive: 'true' | 'false' | '' = '';

  protected readonly currentUser = this.authService.currentUser;

  protected readonly totalCount = computed(() => this.users().length);
  protected readonly activeCount = computed(() => this.users().filter((u) => u.active).length);
  protected readonly inactiveCount = computed(() => this.users().filter((u) => !u.active).length);
  protected readonly adminCount = computed(() => this.users().filter((u) => u.role === 'ADMIN').length);
  protected readonly repCount = computed(() => this.users().filter((u) => u.role === 'SALES_REP').length);

  protected readonly displayedColumns: string[] = [
    'user',
    'email',
    'role',
    'status',
    'createdAt',
    'actions'
  ];

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.loading.set(true);
    this.error.set(null);

    const activeParam = this.selectedActive === 'true' ? true : this.selectedActive === 'false' ? false : '';

    this.userService.getAdminUsers(this.searchTerm, this.selectedRole, activeParam).subscribe({
      next: (data) => {
        this.users.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        const msg = err.error?.message || 'Failed to load system users.';
        this.error.set(msg);
        this.loading.set(false);
      }
    });
  }

  onFilterChange(): void {
    this.loadUsers();
  }

  clearFilters(): void {
    this.searchTerm = '';
    this.selectedRole = '';
    this.selectedActive = '';
    this.loadUsers();
  }

  openCreateDialog(): void {
    const dialogRef = this.dialog.open(UserDialogComponent, {
      width: '520px',
      data: { mode: 'create' }
    });

    dialogRef.afterClosed().subscribe((result: UserResponse | undefined) => {
      if (result) {
        this.snackBar.open(`User "${result.fullName}" created successfully!`, 'Close', {
          duration: 3500
        });
        this.loadUsers();
      }
    });
  }

  openEditDialog(user: UserResponse): void {
    const dialogRef = this.dialog.open(UserDialogComponent, {
      width: '520px',
      data: { mode: 'edit', user }
    });

    dialogRef.afterClosed().subscribe((result: UserResponse | undefined) => {
      if (result) {
        this.snackBar.open(`User "${result.fullName}" updated!`, 'Close', {
          duration: 3000
        });
        this.loadUsers();
      }
    });
  }

  toggleActive(user: UserResponse): void {
    const actionWord = user.active ? 'deactivate' : 'activate';
    if (!confirm(`Are you sure you want to ${actionWord} account for "${user.fullName}" (${user.email})?`)) {
      return;
    }

    const action$ = user.active
      ? this.userService.deactivateUser(user.id)
      : this.userService.activateUser(user.id);

    action$.subscribe({
      next: (updated) => {
        const statusMsg = updated.active ? 'activated' : 'deactivated';
        this.snackBar.open(`User account ${statusMsg} successfully!`, 'Close', {
          duration: 3000
        });
        this.loadUsers();
      },
      error: (err) => {
        const msg = err.error?.message || `Failed to ${actionWord} user.`;
        this.snackBar.open(msg, 'Close', { duration: 4000 });
      }
    });
  }
}

