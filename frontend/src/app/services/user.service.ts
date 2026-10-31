import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { CreateUserRequest, UpdateUserRequest, UserResponse, UserSummary } from '../models/user.models';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/api/users`;
  private readonly adminUrl = `${environment.apiUrl}/api/admin/users`;

  getUsers(): Observable<UserSummary[]> {
    return this.http.get<UserSummary[]>(this.baseUrl);
  }

  getAdminUsers(
    search?: string,
    role?: string,
    active?: boolean | ''
  ): Observable<UserResponse[]> {
    let params = new HttpParams();
    if (search && search.trim()) {
      params = params.set('search', search.trim());
    }
    if (role && role.trim()) {
      params = params.set('role', role.trim());
    }
    if (active !== undefined && active !== null && active !== '') {
      params = params.set('active', active.toString());
    }

    return this.http.get<UserResponse[]>(this.adminUrl, { params });
  }

  getUserById(id: number): Observable<UserResponse> {
    return this.http.get<UserResponse>(`${this.adminUrl}/${id}`);
  }

  createUser(request: CreateUserRequest): Observable<UserResponse> {
    return this.http.post<UserResponse>(this.adminUrl, request);
  }

  updateUser(id: number, request: UpdateUserRequest): Observable<UserResponse> {
    return this.http.put<UserResponse>(`${this.adminUrl}/${id}`, request);
  }

  activateUser(id: number): Observable<UserResponse> {
    return this.http.patch<UserResponse>(`${this.adminUrl}/${id}/activate`, {});
  }

  deactivateUser(id: number): Observable<UserResponse> {
    return this.http.patch<UserResponse>(`${this.adminUrl}/${id}/deactivate`, {});
  }
}
