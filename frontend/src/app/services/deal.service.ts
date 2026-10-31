import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Deal, DealRequest, DealStage } from '../models/deal.models';
import { Lead } from '../models/lead.models';

@Injectable({
  providedIn: 'root'
})
export class DealService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/api/deals`;

  getDeals(
    search?: string,
    stage?: DealStage | '',
    assignedUserId?: number | ''
  ): Observable<Deal[]> {
    let params = new HttpParams();
    if (search && search.trim()) {
      params = params.set('search', search.trim());
    }
    if (stage && stage.trim()) {
      params = params.set('stage', stage.trim());
    }
    if (assignedUserId !== undefined && assignedUserId !== null && assignedUserId !== '') {
      params = params.set('assignedUserId', assignedUserId.toString());
    }

    return this.http.get<Deal[]>(this.baseUrl, { params });
  }

  getDealById(id: number): Observable<Deal> {
    return this.http.get<Deal>(`${this.baseUrl}/${id}`);
  }

  createDeal(request: DealRequest): Observable<Deal> {
    return this.http.post<Deal>(this.baseUrl, request);
  }

  updateDeal(id: number, request: DealRequest): Observable<Deal> {
    return this.http.put<Deal>(`${this.baseUrl}/${id}`, request);
  }

  deleteDeal(id: number): Observable<{ status: number; message: string }> {
    return this.http.delete<{ status: number; message: string }>(`${this.baseUrl}/${id}`);
  }

  getAvailableLeads(): Observable<Lead[]> {
    return this.http.get<Lead[]>(`${this.baseUrl}/available-leads`);
  }
}

