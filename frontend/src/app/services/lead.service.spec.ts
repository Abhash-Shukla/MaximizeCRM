import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { LeadService } from './lead.service';
import { Lead, LeadRequest } from '../models/lead.models';

describe('LeadService', () => {
  let service: LeadService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.resetTestingModule();

    TestBed.configureTestingModule({
      providers: [
        LeadService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });

    service = TestBed.inject(LeadService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should fetch leads with search, status, and assignedUserId filters', () => {
    const mockLeads: Lead[] = [
      {
        id: 1,
        contactId: 10,
        contactName: 'Alice Johnson',
        assignedUserId: 20,
        assignedUserName: 'Sarah Rep',
        assignedUserEmail: 'sarah@maximizecrm.local',
        status: 'NEW',
        source: 'Website',
        reminderId: 100,
        reminderStatus: 'PENDING'
      }
    ];

    service.getLeads('Alice', 'NEW', 20).subscribe((leads) => {
      expect(leads.length).toBe(1);
      expect(leads[0].contactName).toBe('Alice Johnson');
      expect(leads[0].status).toBe('NEW');
      expect(leads[0].reminderStatus).toBe('PENDING');
    });

    const req = httpMock.expectOne('/api/leads?search=Alice&status=NEW&assignedUserId=20');
    expect(req.request.method).toBe('GET');
    req.flush(mockLeads);
  });

  it('should create a new lead', () => {
    const request: LeadRequest = {
      contactId: 10,
      assignedUserId: 20,
      status: 'NEW',
      source: 'Website',
      notes: 'Initial inquiry'
    };

    const mockResponse: Lead = {
      id: 5,
      ...request,
      contactName: 'Alice Johnson',
      assignedUserName: 'Sarah Rep',
      assignedUserEmail: 'sarah@maximizecrm.local',
      reminderId: 105,
      reminderStatus: 'PENDING'
    };

    service.createLead(request).subscribe((lead) => {
      expect(lead.id).toBe(5);
      expect(lead.reminderId).toBe(105);
      expect(lead.reminderStatus).toBe('PENDING');
    });

    const req = httpMock.expectOne('/api/leads');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush(mockResponse);
  });

  it('should update an existing lead', () => {
    const updateRequest: LeadRequest = {
      contactId: 10,
      assignedUserId: 21,
      status: 'QUALIFIED',
      source: 'Phone',
      notes: 'Demo completed'
    };

    const mockResponse: Lead = {
      id: 5,
      ...updateRequest,
      contactName: 'Alice Johnson',
      assignedUserName: 'Jane Rep',
      assignedUserEmail: 'jane@maximizecrm.local'
    };

    service.updateLead(5, updateRequest).subscribe((lead) => {
      expect(lead.status).toBe('QUALIFIED');
      expect(lead.source).toBe('Phone');
    });

    const req = httpMock.expectOne('/api/leads/5');
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(updateRequest);
    req.flush(mockResponse);
  });

  it('should delete a lead by id', () => {
    service.deleteLead(5).subscribe((res) => {
      expect(res.status).toBe(200);
      expect(res.message).toBe('Lead deleted successfully');
    });

    const req = httpMock.expectOne('/api/leads/5');
    expect(req.request.method).toBe('DELETE');
    req.flush({ status: 200, message: 'Lead deleted successfully' });
  });

  it('should get lead by id', () => {
    const mockLead: Lead = {
      id: 8,
      contactId: 10,
      contactName: 'Bob',
      assignedUserId: 20,
      assignedUserName: 'Rep',
      assignedUserEmail: 'rep@test.com',
      status: 'CONTACTED',
      source: 'Walk-in'
    };

    service.getLeadById(8).subscribe((lead) => {
      expect(lead.id).toBe(8);
      expect(lead.status).toBe('CONTACTED');
    });

    const req = httpMock.expectOne('/api/leads/8');
    expect(req.request.method).toBe('GET');
    req.flush(mockLead);
  });
});

