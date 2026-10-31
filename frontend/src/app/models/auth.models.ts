export type Role = 'ADMIN' | 'SALES_REP';

export interface User {
  id: number;
  fullName: string;
  email: string;
  role: Role;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  id: number;
  fullName: string;
  email: string;
  role: Role;
}

