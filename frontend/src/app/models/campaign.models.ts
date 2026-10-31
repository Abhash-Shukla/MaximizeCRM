export type CampaignType = 'EMAIL' | 'WEBINAR' | 'SOCIAL_MEDIA' | 'EVENT' | 'NEWSLETTER' | 'OTHER';

export type CampaignStatus = 'PLANNING' | 'ACTIVE' | 'COMPLETED' | 'CANCELLED';

export interface Campaign {
  id: number;
  name: string;
  type: CampaignType;
  status: CampaignStatus;
  startDate?: string | null;
  endDate?: string | null;
  budget?: number | null;
  expectedRevenue?: number | null;
  actualCost?: number | null;
  assignedUserId: number;
  assignedUserName?: string | null;
  assignedUserEmail?: string | null;
  description?: string | null;
  createdAt?: string;
  updatedAt?: string;
}

export interface CampaignRequest {
  name: string;
  type?: CampaignType;
  status?: CampaignStatus;
  startDate?: string | null;
  endDate?: string | null;
  budget?: number | null;
  expectedRevenue?: number | null;
  actualCost?: number | null;
  assignedUserId: number;
  description?: string | null;
}

