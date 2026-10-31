import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { UserService } from './user.service';
import { CreateUserRequest, UpdateUserRequest, UserResponse, UserSummary } from '../models/user.models';

describe('UserService', () => {
  let service: UserService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.resetTestingModule();

    TestBed.configureTestingModule({
      providers: [
        UserService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });

    service = TestBed.inject(UserService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should fetch assignable users from /api/users', () => {
    const mockUsers: UserSummary[] = [
      { id: 1, fullName: 'Admin User', email: 'admin@maximizecrm.local', role: 'ADMIN', active: true }
    ];

    service.getUsers().subscribe((users) => {
      expect(users.length).toBe(1);
      expect(users[0].fullName).toBe('Admin User');
    });

    const req = httpMock.expectOne('/api/users');
    expect(req.request.method).toBe('GET');
    req.flush(mockUsers);
  });

  it('should fetch admin users with search and filter parameters from /api/admin/users', () => {
    const mockUsers: UserResponse[] = [
      { id: 2, fullName: 'Sarah Rep', email: 'rep@maximizecrm.local', role: 'SALES_REP', active: true, createdAt: '2026-05-01' }
    ];

    service.getAdminUsers('Sarah', 'SALES_REP', true).subscribe((users) => {
      expect(users.length).toBe(1);
      expect(users[0].fullName).toBe('Sarah Rep');
    });

    const req = httpMock.expectOne('/api/admin/users?search=Sarah&role=SALES_REP&active=true');
    expect(req.request.method).toBe('GET');
    req.flush(mockUsers);
  });

  it('should create a new user via POST /api/admin/users', () => {
    const request: CreateUserRequest = {
      fullName: 'New User',
      email: 'new@maximizecrm.local',
      password: 'Password@123',
      role: 'SALES_REP',
      active: true
    };

    const mockResponse: UserResponse = {
      id: 10,
      fullName: request.fullName,
      email: request.email,
      role: request.role,
      active: true
    };

    service.createUser(request).subscribe((user) => {
      expect(user.id).toBe(10);
      expect(user.email).toBe('new@maximizecrm.local');
    });

    const req = httpMock.expectOne('/api/admin/users');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush(mockResponse);
  });

  it('should update an existing user via PUT /api/admin/users/:id', () => {
    const request: UpdateUserRequest = {
      fullName: 'Updated Name',
      email: 'updated@maximizecrm.local',
      role: 'ADMIN',
      active: true
    };

    const mockResponse: UserResponse = {
      id: 10,
      fullName: request.fullName,
      email: request.email,
      role: request.role,
      active: true
    };

    service.updateUser(10, request).subscribe((user) => {
      expect(user.id).toBe(10);
      expect(user.fullName).toBe('Updated Name');
    });

    const req = httpMock.expectOne('/api/admin/users/10');
    expect(req.request.method).toBe('PUT');
    req.flush(mockResponse);
  });

  it('should deactivate a user via PATCH /api/admin/users/:id/deactivate', () => {
    const mockResponse: UserResponse = {
      id: 10,
      fullName: 'Deactivated User',
      email: 'user@maximizecrm.local',
      role: 'SALES_REP',
      active: false
    };

    service.deactivateUser(10).subscribe((user) => {
      expect(user.active).toBe(false);
    });

    const req = httpMock.expectOne('/api/admin/users/10/deactivate');
    expect(req.request.method).toBe('PATCH');
    req.flush(mockResponse);
  });
});

