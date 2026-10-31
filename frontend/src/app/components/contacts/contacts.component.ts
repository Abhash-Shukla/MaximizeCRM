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

import { Contact, ContactCategory } from '../../models/contact.models';
import { ContactService } from '../../services/contact.service';
import { ContactDialogComponent } from './contact-dialog.component';

@Component({
  selector: 'app-contacts',
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
  templateUrl: './contacts.component.html',
  styleUrl: './contacts.component.scss'
})
export class ContactsComponent implements OnInit {
  private readonly contactService = inject(ContactService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  protected readonly contacts = signal<Contact[]>([]);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);

  protected searchTerm = '';
  protected selectedCategory: ContactCategory | '' = '';

  protected readonly displayedColumns: string[] = [
    'name',
    'category',
    'email',
    'phone',
    'company',
    'createdAt',
    'actions'
  ];

  ngOnInit(): void {
    this.loadContacts();
  }

  loadContacts(): void {
    this.loading.set(true);
    this.error.set(null);

    this.contactService.getContacts(this.searchTerm, this.selectedCategory).subscribe({
      next: (data) => {
        this.contacts.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        const msg = err.error?.message || 'Failed to load contacts.';
        this.error.set(msg);
        this.loading.set(false);
      }
    });
  }

  onSearchChange(): void {
    this.loadContacts();
  }

  onCategoryChange(): void {
    this.loadContacts();
  }

  clearFilters(): void {
    this.searchTerm = '';
    this.selectedCategory = '';
    this.loadContacts();
  }

  openCreateDialog(): void {
    const dialogRef = this.dialog.open(ContactDialogComponent, {
      width: '560px',
      data: { mode: 'create' }
    });

    dialogRef.afterClosed().subscribe((result: Contact | undefined) => {
      if (result) {
        this.snackBar.open(`Contact "${result.name}" created successfully.`, 'Close', {
          duration: 3500,
          horizontalPosition: 'end',
          verticalPosition: 'top'
        });
        this.loadContacts();
      }
    });
  }

  openEditDialog(contact: Contact): void {
    const dialogRef = this.dialog.open(ContactDialogComponent, {
      width: '560px',
      data: { mode: 'edit', contact }
    });

    dialogRef.afterClosed().subscribe((result: Contact | undefined) => {
      if (result) {
        this.snackBar.open(`Contact "${result.name}" updated successfully.`, 'Close', {
          duration: 3500,
          horizontalPosition: 'end',
          verticalPosition: 'top'
        });
        this.loadContacts();
      }
    });
  }

  deleteContact(contact: Contact): void {
    const confirmed = window.confirm(`Are you sure you want to delete contact "${contact.name}"?`);
    if (!confirmed) {
      return;
    }

    this.contactService.deleteContact(contact.id).subscribe({
      next: () => {
        this.snackBar.open(`Contact "${contact.name}" deleted successfully.`, 'Close', {
          duration: 3500,
          horizontalPosition: 'end',
          verticalPosition: 'top'
        });
        this.loadContacts();
      },
      error: (err) => {
        const msg = err.error?.message || 'Failed to delete contact.';
        this.snackBar.open(msg, 'Dismiss', {
          duration: 6000,
          horizontalPosition: 'end',
          verticalPosition: 'top',
          panelClass: ['error-snackbar']
        });
      }
    });
  }
}

