import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { CampaignService } from './campaign.service';
import { Campaign, CampaignRequest } from '../models/campaign.models';

describe('CampaignService', () => {
  let service: CampaignService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.resetTestingModule();

    TestBed.configureTestingModule({
      providers: [
        CampaignService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });

    service = TestBed.inject(CampaignService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should fetch campaigns with search, status, and assignedUserId parameters', () => {
    const mockCampaigns: Campaign[] = [
      {
        id: 1,
        name: 'Spring Product Launch',
        type: 'EMAIL',
        status: 'ACTIVE',
        startDate: '2026-03-01',
        endDate: '2026-03-31',
        budget: 5000,
        expectedRevenue: 25000,
        actualCost: 1200,
        assignedUserId: 1,
        assignedUserName: 'Admin User',
        description: 'Email newsletter launch',
        createdAt: '2026-02-01T10:00:00Z',
        updatedAt: '2026-02-01T10:00:00Z'
      }
    ];

    service.getCampaigns('Spring', 'ACTIVE', 1).subscribe((campaigns) => {
      expect(campaigns.length).toBe(1);
      expect(campaigns[0].name).toBe('Spring Product Launch');
      expect(campaigns[0].status).toBe('ACTIVE');
      expect(campaigns[0].budget).toBe(5000);
    });

    const req = httpMock.expectOne('/api/campaigns?search=Spring&status=ACTIVE&assignedUserId=1');
    expect(req.request.method).toBe('GET');
    req.flush(mockCampaigns);
  });

  it('should fetch a single campaign by ID', () => {
    const mockCampaign: Campaign = {
      id: 5,
      name: 'Tech Expo Webinar',
      type: 'WEBINAR',
      status: 'PLANNING',
      budget: 10000,
      expectedRevenue: 40000,
      assignedUserId: 2,
      assignedUserName: 'John Doe'
    };

    service.getCampaignById(5).subscribe((campaign) => {
      expect(campaign.id).toBe(5);
      expect(campaign.name).toBe('Tech Expo Webinar');
      expect(campaign.type).toBe('WEBINAR');
    });

    const req = httpMock.expectOne('/api/campaigns/5');
    expect(req.request.method).toBe('GET');
    req.flush(mockCampaign);
  });

  it('should create a new campaign', () => {
    const request: CampaignRequest = {
      name: 'Q3 Enterprise Outreach',
      type: 'EMAIL',
      status: 'PLANNING',
      budget: 8000,
      expectedRevenue: 30000,
      assignedUserId: 1,
      description: 'Enterprise outreach initiative'
    };

    const mockResponse: Campaign = {
      id: 10,
      name: request.name,
      type: 'EMAIL',
      status: 'PLANNING',
      budget: request.budget,
      expectedRevenue: request.expectedRevenue,
      assignedUserId: request.assignedUserId,
      description: request.description,
      assignedUserName: 'Admin User',
      actualCost: 0,
      createdAt: '2026-03-01T12:00:00Z'
    };

    service.createCampaign(request).subscribe((campaign) => {
      expect(campaign.id).toBe(10);
      expect(campaign.name).toBe('Q3 Enterprise Outreach');
      expect(campaign.budget).toBe(8000);
    });

    const req = httpMock.expectOne('/api/campaigns');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush(mockResponse);
  });

  it('should update an existing campaign', () => {
    const updateRequest: CampaignRequest = {
      name: 'Q3 Enterprise Outreach - Updated',
      type: 'EMAIL',
      status: 'ACTIVE',
      budget: 9500,
      expectedRevenue: 35000,
      assignedUserId: 1
    };

    const mockResponse: Campaign = {
      id: 10,
      name: updateRequest.name,
      type: 'EMAIL',
      status: 'ACTIVE',
      budget: updateRequest.budget,
      expectedRevenue: updateRequest.expectedRevenue,
      assignedUserId: updateRequest.assignedUserId,
      assignedUserName: 'Admin User'
    };

    service.updateCampaign(10, updateRequest).subscribe((campaign) => {
      expect(campaign.id).toBe(10);
      expect(campaign.name).toBe('Q3 Enterprise Outreach - Updated');
      expect(campaign.status).toBe('ACTIVE');
      expect(campaign.budget).toBe(9500);
    });

    const req = httpMock.expectOne('/api/campaigns/10');
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(updateRequest);
    req.flush(mockResponse);
  });

  it('should delete a campaign', () => {
    const mockDeleteResponse = {
      status: 200,
      message: 'Campaign deleted successfully.'
    };

    service.deleteCampaign(10).subscribe((res) => {
      expect(res.status).toBe(200);
      expect(res.message).toBe('Campaign deleted successfully.');
    });

    const req = httpMock.expectOne('/api/campaigns/10');
    expect(req.request.method).toBe('DELETE');
    req.flush(mockDeleteResponse);
  });
});
