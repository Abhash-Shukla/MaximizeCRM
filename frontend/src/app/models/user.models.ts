export interface UserSummary {
  id: number;
  fullName: string;
  email: string;
  role: 'ADMIN' | 'SALES_REP';
  active: boolean;
}

