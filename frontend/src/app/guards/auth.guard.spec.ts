import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { authGuard, adminGuard } from './auth.guard';
import { AuthService } from '../services/auth.service';
import { signal } from '@angular/core';
import { UserSummary } from '../models/user.models';

describe('Auth Guards', () => {
  let mockAuthService: {
    isLoggedIn: ReturnType<typeof vi.fn>;
    currentUser: ReturnType<typeof signal<UserSummary | null>>;
  };
  let mockRouter: {
    navigate: ReturnType<typeof vi.fn>;
  };

  beforeEach(() => {
    mockAuthService = {
      isLoggedIn: vi.fn(),
      currentUser: signal<UserSummary | null>(null)
    };

    mockRouter = {
      navigate: vi.fn()
    };

    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: mockAuthService },
        { provide: Router, useValue: mockRouter }
      ]
    });
  });

  describe('authGuard', () => {
    it('should allow navigation when user is logged in', () => {
      mockAuthService.isLoggedIn.mockReturnValue(true);

      const result = TestBed.runInInjectionContext(() => authGuard({} as any, {} as any));
      expect(result).toBe(true);
      expect(mockRouter.navigate).not.toHaveBeenCalled();
    });

    it('should redirect to /login when user is not logged in', () => {
      mockAuthService.isLoggedIn.mockReturnValue(false);

      const result = TestBed.runInInjectionContext(() => authGuard({} as any, {} as any));
      expect(result).toBe(false);
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/login']);
    });
  });

  describe('adminGuard', () => {
    it('should allow navigation when user is logged in with ADMIN role', () => {
      mockAuthService.isLoggedIn.mockReturnValue(true);
      mockAuthService.currentUser.set({
        id: 1,
        fullName: 'Admin User',
        email: 'admin@maximizecrm.com',
        role: 'ADMIN',
        active: true
      });

      const result = TestBed.runInInjectionContext(() => adminGuard({} as any, {} as any));
      expect(result).toBe(true);
      expect(mockRouter.navigate).not.toHaveBeenCalled();
    });

    it('should redirect to /dashboard when user is logged in with SALES_REP role', () => {
      mockAuthService.isLoggedIn.mockReturnValue(true);
      mockAuthService.currentUser.set({
        id: 2,
        fullName: 'Sales Rep User',
        email: 'rep@maximizecrm.com',
        role: 'SALES_REP',
        active: true
      });

      const result = TestBed.runInInjectionContext(() => adminGuard({} as any, {} as any));
      expect(result).toBe(false);
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/dashboard']);
    });

    it('should redirect to /dashboard when user is not logged in', () => {
      mockAuthService.isLoggedIn.mockReturnValue(false);
      mockAuthService.currentUser.set(null);

      const result = TestBed.runInInjectionContext(() => adminGuard({} as any, {} as any));
      expect(result).toBe(false);
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/dashboard']);
    });
  });
});
