import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { AuthService } from './auth.service';
import { LoginRequest, LoginResponse } from '../models/auth.models';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.resetTestingModule();
    sessionStorage.clear();

    TestBed.configureTestingModule({
      providers: [
        AuthService,
        provideRouter([{ path: 'login', component: class {} }]),
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });

    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    sessionStorage.clear();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
    expect(service.isLoggedIn()).toBe(false);
  });

  it('should authenticate user and store token in sessionStorage on login', () => {
    const mockCredentials: LoginRequest = {
      email: 'admin@maximizecrm.local',
      password: 'Admin@123'
    };

    const mockResponse: LoginResponse = {
      token: 'mock-jwt-token',
      id: 1,
      fullName: 'System Administrator',
      email: 'admin@maximizecrm.local',
      role: 'ADMIN'
    };

    service.login(mockCredentials).subscribe((res) => {
      expect(res.token).toEqual('mock-jwt-token');
      expect(service.isLoggedIn()).toBe(true);
      expect(service.getToken()).toEqual('mock-jwt-token');
      expect(sessionStorage.getItem('auth_token')).toEqual('mock-jwt-token');
      expect(service.getUser()?.email).toEqual('admin@maximizecrm.local');
    });

    const req = httpMock.expectOne('/api/auth/login');
    expect(req.request.method).toBe('POST');
    req.flush(mockResponse);
  });

  it('should clear sessionStorage and signals on logout', () => {
    sessionStorage.setItem('auth_token', 'sample-token');
    sessionStorage.setItem('auth_user', JSON.stringify({ id: 1, fullName: 'Admin', email: 'admin@test.com', role: 'ADMIN' }));

    service.logout();

    expect(sessionStorage.getItem('auth_token')).toBeNull();
    expect(sessionStorage.getItem('auth_user')).toBeNull();
    expect(service.isLoggedIn()).toBe(false);
    expect(service.getUser()).toBeNull();
  });
});
