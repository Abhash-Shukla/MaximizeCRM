import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Campaign, CampaignRequest, CampaignStatus } from '../models/campaign.models';

@Injectable({
  providedIn: 'root'
})
export class CampaignService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/api/campaigns`;

  getCampaigns(
    search?: string,
    status?: CampaignStatus | '',
    assignedUserId?: number | ''
  ): Observable<Campaign[]> {
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

    return this.http.get<Campaign[]>(this.baseUrl, { params });
  }

  getCampaignById(id: number): Observable<Campaign> {
    return this.http.get<Campaign>(`${this.baseUrl}/${id}`);
  }

  createCampaign(request: CampaignRequest): Observable<Campaign> {
    return this.http.post<Campaign>(this.baseUrl, request);
  }

  updateCampaign(id: number, request: CampaignRequest): Observable<Campaign> {
    return this.http.put<Campaign>(`${this.baseUrl}/${id}`, request);
  }

  deleteCampaign(id: number): Observable<{ status: number; message: string }> {
    return this.http.delete<{ status: number; message: string }>(`${this.baseUrl}/${id}`);
  }
}

