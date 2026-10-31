export type UserRole = 'ADMIN' | 'SALES_REP';

export interface UserSummary {
  id: number;
  fullName: string;
  email: string;
  role: UserRole;
  active: boolean;
}

export interface UserResponse {
  id: number;
  fullName: string;
  email: string;
  role: UserRole;
  active: boolean;
  createdAt?: string;
}

export interface CreateUserRequest {
  fullName: string;
  email: string;
  password: string;
  role: UserRole;
  active?: boolean;
}

export interface UpdateUserRequest {
  fullName: string;
  email: string;
  password?: string | null;
  role: UserRole;
  active?: boolean;
}
