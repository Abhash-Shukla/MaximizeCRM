export type ContactCategory = 'NEW' | 'REGULAR' | 'VIP';

export interface Contact {
  id: number;
  name: string;
  email?: string | null;
  phone?: string | null;
  category: ContactCategory;
  company?: string | null;
  notes?: string | null;
  createdAt?: string;
  updatedAt?: string;
}

export interface ContactRequest {
  name: string;
  email?: string | null;
  phone?: string | null;
  category: ContactCategory;
  company?: string | null;
  notes?: string | null;
}

