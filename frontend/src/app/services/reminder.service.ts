import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Reminder, ReminderRequest, ReminderStatus } from '../models/reminder.models';

@Injectable({
  providedIn: 'root'
})
export class ReminderService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/api/reminders`;

  getReminders(
    search?: string,
    status?: ReminderStatus | '',
    assignedUserId?: number | '',
    leadId?: number | ''
  ): Observable<Reminder[]> {
    let params = new HttpParams();
    if (search && search.trim()) {
      params = params.set('search', search.trim());
    }
    if (status && status.trim()) {
      params = params.set('status', status.trim());
    }
    if (assignedUserId !== undefined && assignedUserId !== null && assignedUserId !== '') {
      params = params.set('assignedUserId', assignedUserId.toString());
    }
    if (leadId !== undefined && leadId !== null && leadId !== '') {
      params = params.set('leadId', leadId.toString());
    }

    return this.http.get<Reminder[]>(this.baseUrl, { params });
  }

  getReminderById(id: number): Observable<Reminder> {
    return this.http.get<Reminder>(`${this.baseUrl}/${id}`);
  }

  createReminder(request: ReminderRequest): Observable<Reminder> {
    return this.http.post<Reminder>(this.baseUrl, request);
  }

  updateReminder(id: number, request: ReminderRequest): Observable<Reminder> {
    return this.http.put<Reminder>(`${this.baseUrl}/${id}`, request);
  }

  completeReminder(id: number): Observable<Reminder> {
    return this.http.patch<Reminder>(`${this.baseUrl}/${id}/complete`, {});
  }

  deleteReminder(id: number): Observable<{ status: number; message: string }> {
    return this.http.delete<{ status: number; message: string }>(`${this.baseUrl}/${id}`);
  }
}

