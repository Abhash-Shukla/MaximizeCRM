import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatDividerModule } from '@angular/material/divider';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AuthService } from '../../services/auth.service';
import { HealthService, HealthResponse } from '../../services/health.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatDividerModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent {
  protected readonly authService = inject(AuthService);
  private readonly healthService = inject(HealthService);

  protected readonly user = this.authService.currentUser;

  // Protected endpoint test state
  protected readonly meData = signal<any | null>(null);
  protected readonly meLoading = signal<boolean>(false);
  protected readonly meError = signal<string | null>(null);

  // Health endpoint test state
  protected readonly healthData = signal<HealthResponse | null>(null);
  protected readonly healthLoading = signal<boolean>(false);
  protected readonly healthError = signal<string | null>(null);

  testProtectedApi(): void {
    this.meLoading.set(true);
    this.meError.set(null);

    this.authService.getMe().subscribe({
      next: (data) => {
        this.meData.set(data);
        this.meLoading.set(false);
      },
      error: (err) => {
        this.meError.set(err.error?.message || `Failed with status ${err.status}`);
        this.meLoading.set(false);
      }
    });
  }

  testHealthCheck(): void {
    this.healthLoading.set(true);
    this.healthError.set(null);

    this.healthService.checkHealth().subscribe({
      next: (data) => {
        this.healthData.set(data);
        this.healthLoading.set(false);
      },
      error: (err) => {
        this.healthError.set(`Connection failed: ${err.message || 'Unable to reach backend'}`);
        this.healthLoading.set(false);
      }
    });
  }

  logout(): void {
    this.authService.logout();
  }
}

