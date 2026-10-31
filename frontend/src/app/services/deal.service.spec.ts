import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { DealService } from './deal.service';
import { Deal, DealRequest } from '../models/deal.models';
import { Lead } from '../models/lead.models';

describe('DealService', () => {
  let service: DealService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.resetTestingModule();

    TestBed.configureTestingModule({
      providers: [
        DealService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });

    service = TestBed.inject(DealService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should fetch deals with search, stage, and assignedUserId filters', () => {
    const mockDeals: Deal[] = [
      {
        id: 1,
        name: 'Acme Subscription',
        leadId: 10,
        contactName: 'Alice Buyer',
        assignedUserId: 20,
        assignedUserName: 'Sarah Rep',
        stage: 'PROSPECT',
        amount: 25000,
        closedAt: null
      }
    ];

    service.getDeals('Acme', 'PROSPECT', 20).subscribe((deals) => {
      expect(deals.length).toBe(1);
      expect(deals[0].name).toBe('Acme Subscription');
      expect(deals[0].stage).toBe('PROSPECT');
      expect(deals[0].amount).toBe(25000);
    });

    const req = httpMock.expectOne('/api/deals?search=Acme&stage=PROSPECT&assignedUserId=20');
    expect(req.request.method).toBe('GET');
    req.flush(mockDeals);
  });

  it('should create a new deal', () => {
    const request: DealRequest = {
      name: 'Cloud Migration',
      leadId: 10,
      assignedUserId: 20,
      stage: 'PROSPECT',
      amount: 15000,
      notes: 'Initial scope'
    };

    const mockResponse: Deal = {
      id: 2,
      name: request.name,
      leadId: request.leadId,
      assignedUserId: request.assignedUserId,
      stage: 'PROSPECT',
      amount: request.amount,
      notes: request.notes,
      contactName: 'Bob Client',
      assignedUserName: 'Sarah Rep',
      closedAt: null
    };

    service.createDeal(request).subscribe((deal) => {
      expect(deal.id).toBe(2);
      expect(deal.stage).toBe('PROSPECT');
      expect(deal.amount).toBe(15000);
    });

    const req = httpMock.expectOne('/api/deals');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush(mockResponse);
  });

  it('should update an existing deal', () => {
    const updateRequest: DealRequest = {
      name: 'Cloud Migration',
      leadId: 10,
      assignedUserId: 20,
      stage: 'NEGOTIATION',
      amount: 18000
    };

    const mockResponse: Deal = {
      id: 2,
      name: updateRequest.name,
      leadId: updateRequest.leadId,
      assignedUserId: updateRequest.assignedUserId,
      stage: 'NEGOTIATION',
      amount: updateRequest.amount,
      notes: updateRequest.notes,
      contactName: 'Bob Client',
      assignedUserName: 'Sarah Rep',
      closedAt: null
    };

    service.updateDeal(2, updateRequest).subscribe((deal) => {
      expect(deal.stage).toBe('NEGOTIATION');
      expect(deal.amount).toBe(18000);
    });

    const req = httpMock.expectOne('/api/deals/2');
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(updateRequest);
    req.flush(mockResponse);
  });

  it('should delete a deal', () => {
    service.deleteDeal(2).subscribe((res) => {
      expect(res.message).toBe('Deal deleted successfully');
    });

    const req = httpMock.expectOne('/api/deals/2');
    expect(req.request.method).toBe('DELETE');
    req.flush({ status: 200, message: 'Deal deleted successfully' });
  });

  it('should fetch available leads eligible for deal creation', () => {
    const mockLeads: Lead[] = [
      {
        id: 15,
        contactId: 5,
        contactName: 'Charlie Lead',
        assignedUserId: 20,
        assignedUserName: 'Sarah Rep',
        assignedUserEmail: 'sarah@test.com',
        status: 'QUALIFIED',
        source: 'Website'
      }
    ];

    service.getAvailableLeads().subscribe((leads) => {
      expect(leads.length).toBe(1);
      expect(leads[0].id).toBe(15);
      expect(leads[0].contactName).toBe('Charlie Lead');
    });

    const req = httpMock.expectOne('/api/deals/available-leads');
    expect(req.request.method).toBe('GET');
    req.flush(mockLeads);
  });
});
