import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';

import { MatToolbarModule } from '@angular/material/toolbar';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { HealthService, HealthResponse } from './services/health.service';
import { AuthService } from './services/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    MatToolbarModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App {
  private readonly healthService = inject(HealthService);
  protected readonly authService = inject(AuthService);

  protected readonly title = signal('MaximizeCRM');
  protected readonly healthData = signal<HealthResponse | null>(null);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly currentUser = this.authService.currentUser;
  protected readonly isAuthenticated = this.authService.isAuthenticated;

  checkBackend(): void {
    this.loading.set(true);
    this.error.set(null);

    this.healthService.checkHealth().subscribe({
      next: (data) => {
        this.healthData.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(
          `Connection failed: ${err.message || 'Unable to reach backend at http://localhost:8080'}`
        );
        this.loading.set(false);
      }
    });
  }

  logout(): void {
    this.authService.logout();
  }
}