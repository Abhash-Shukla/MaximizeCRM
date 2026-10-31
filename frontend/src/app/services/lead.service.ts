import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Lead, LeadRequest, LeadStatus } from '../models/lead.models';

@Injectable({
  providedIn: 'root'
})
export class LeadService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/api/leads`;

  getLeads(
    search?: string,
    status?: LeadStatus | '',
    assignedUserId?: number | ''
  ): Observable<Lead[]> {
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

    return this.http.get<Lead[]>(this.baseUrl, { params });
  }

  getLeadById(id: number): Observable<Lead> {
    return this.http.get<Lead>(`${this.baseUrl}/${id}`);
  }

  createLead(request: LeadRequest): Observable<Lead> {
    return this.http.post<Lead>(this.baseUrl, request);
  }

  updateLead(id: number, request: LeadRequest): Observable<Lead> {
    return this.http.put<Lead>(`${this.baseUrl}/${id}`, request);
  }

  deleteLead(id: number): Observable<{ status: number; message: string }> {
    return this.http.delete<{ status: number; message: string }>(`${this.baseUrl}/${id}`);
  }
}

