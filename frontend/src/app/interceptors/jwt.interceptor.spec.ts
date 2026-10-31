import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { jwtInterceptor } from './jwt.interceptor';
import { AuthService } from '../services/auth.service';

describe('jwtInterceptor', () => {
  let http: HttpClient;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.resetTestingModule();
    sessionStorage.clear();

    TestBed.configureTestingModule({
      providers: [
        AuthService,
        provideHttpClient(withInterceptors([jwtInterceptor])),
        provideHttpClientTesting()
      ]
    });

    http = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    sessionStorage.clear();
  });

  it('should attach Authorization Bearer token to /api/leads request when token is present', () => {
    sessionStorage.setItem('auth_token', 'valid-test-token-leads');

    http.get('/api/leads').subscribe();

    const req = httpMock.expectOne('/api/leads');
    expect(req.request.headers.has('Authorization')).toBe(true);
    expect(req.request.headers.get('Authorization')).toBe('Bearer valid-test-token-leads');
    req.flush([]);
  });

  it('should attach Authorization Bearer token to /api/deals request when token is present', () => {
    sessionStorage.setItem('auth_token', 'valid-test-token-deals');

    http.get('/api/deals').subscribe();

    const req = httpMock.expectOne('/api/deals');
    expect(req.request.headers.has('Authorization')).toBe(true);
    expect(req.request.headers.get('Authorization')).toBe('Bearer valid-test-token-deals');
    req.flush([]);
  });

  it('should attach Authorization Bearer token to /api/campaigns request when token is present', () => {
    sessionStorage.setItem('auth_token', 'valid-test-token-campaigns');

    http.get('/api/campaigns').subscribe();

    const req = httpMock.expectOne('/api/campaigns');
    expect(req.request.headers.has('Authorization')).toBe(true);
    expect(req.request.headers.get('Authorization')).toBe('Bearer valid-test-token-campaigns');
    req.flush([]);
  });

  it('should attach Authorization Bearer token to /api/reminders request when token is present', () => {
    sessionStorage.setItem('auth_token', 'valid-test-token-reminders');

    http.get('/api/reminders').subscribe();

    const req = httpMock.expectOne('/api/reminders');
    expect(req.request.headers.has('Authorization')).toBe(true);
    expect(req.request.headers.get('Authorization')).toBe('Bearer valid-test-token-reminders');
    req.flush([]);
  });

  it('should attach Authorization Bearer token to /api/reports/summary request when token is present', () => {
    sessionStorage.setItem('auth_token', 'valid-test-token-reports');

    http.get('/api/reports/summary').subscribe();

    const req = httpMock.expectOne('/api/reports/summary');
    expect(req.request.headers.has('Authorization')).toBe(true);
    expect(req.request.headers.get('Authorization')).toBe('Bearer valid-test-token-reports');
    req.flush({});
  });

  it('should attach Authorization Bearer token to /api/contacts request when token is present', () => {
    sessionStorage.setItem('auth_token', 'valid-test-token-contacts');

    http.get('/api/contacts').subscribe();

    const req = httpMock.expectOne('/api/contacts');
    expect(req.request.headers.has('Authorization')).toBe(true);
    expect(req.request.headers.get('Authorization')).toBe('Bearer valid-test-token-contacts');
    req.flush([]);
  });

  it('should attach Authorization Bearer token to /api/auth/me request when token is present', () => {
    sessionStorage.setItem('auth_token', 'valid-test-token-me');

    http.get('/api/auth/me').subscribe();

    const req = httpMock.expectOne('/api/auth/me');
    expect(req.request.headers.has('Authorization')).toBe(true);
    expect(req.request.headers.get('Authorization')).toBe('Bearer valid-test-token-me');
    req.flush({});
  });

  it('should NOT attach Authorization header to /api/auth/login request', () => {
    sessionStorage.setItem('auth_token', 'sample-token');

    http.post('/api/auth/login', { email: 'test@example.com', password: 'password' }).subscribe();

    const req = httpMock.expectOne('/api/auth/login');
    expect(req.request.headers.has('Authorization')).toBe(false);
    req.flush({});
  });

  it('should NOT attach Authorization header when token is not present', () => {
    http.get('/api/leads').subscribe();

    const req = httpMock.expectOne('/api/leads');
    expect(req.request.headers.has('Authorization')).toBe(false);
    req.flush([]);
  });
});
