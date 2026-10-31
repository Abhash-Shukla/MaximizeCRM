export type DealStage = 'PROSPECT' | 'NEGOTIATION' | 'WON' | 'LOST';

export interface Deal {
  id: number;
  name: string;
  leadId: number;
  leadSource?: string | null;
  leadStatus?: string | null;
  contactId?: number | null;
  contactName?: string | null;
  contactEmail?: string | null;
  contactPhone?: string | null;
  contactCompany?: string | null;
  assignedUserId: number;
  assignedUserName?: string | null;
  assignedUserEmail?: string | null;
  stage: DealStage;
  amount: number;
  notes?: string | null;
  closedAt?: string | null;
  createdAt?: string;
  updatedAt?: string;
}

export interface DealRequest {
  name: string;
  leadId: number;
  assignedUserId: number;
  stage?: DealStage;
  amount: number;
  notes?: string | null;
}

