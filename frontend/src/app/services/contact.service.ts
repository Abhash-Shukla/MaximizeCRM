import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Contact, ContactCategory, ContactRequest } from '../models/contact.models';

@Injectable({
  providedIn: 'root'
})
export class ContactService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/api/contacts`;

  getContacts(search?: string, category?: ContactCategory | ''): Observable<Contact[]> {
    let params = new HttpParams();
    if (search && search.trim()) {
      params = params.set('search', search.trim());
    }
    if (category && category.trim()) {
      params = params.set('category', category.trim());
    }
    return this.http.get<Contact[]>(this.baseUrl, { params });
  }

  getContactById(id: number): Observable<Contact> {
    return this.http.get<Contact>(`${this.baseUrl}/${id}`);
  }

  createContact(request: ContactRequest): Observable<Contact> {
    return this.http.post<Contact>(this.baseUrl, request);
  }

  updateContact(id: number, request: ContactRequest): Observable<Contact> {
    return this.http.put<Contact>(`${this.baseUrl}/${id}`, request);
  }

  deleteContact(id: number): Observable<{ status: number; message: string }> {
    return this.http.delete<{ status: number; message: string }>(`${this.baseUrl}/${id}`);
  }
}

