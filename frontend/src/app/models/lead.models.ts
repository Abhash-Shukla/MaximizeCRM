export type LeadStatus = 'NEW' | 'CONTACTED' | 'QUALIFIED' | 'LOST';

export interface Lead {
  id: number;
  contactId: number;
  contactName: string;
  contactEmail?: string | null;
  contactPhone?: string | null;
  contactCompany?: string | null;
  assignedUserId: number;
  assignedUserName: string;
  assignedUserEmail: string;
  status: LeadStatus;
  source: string;
  notes?: string | null;
  reminderId?: number | null;
  reminderStatus?: string | null;
  createdAt?: string;
  updatedAt?: string;
}

export interface LeadRequest {
  contactId: number;
  assignedUserId: number;
  status: LeadStatus;
  source: string;
  notes?: string | null;
}

