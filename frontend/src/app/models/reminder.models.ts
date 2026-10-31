export type ReminderStatus = 'PENDING' | 'COMPLETED';

export interface Reminder {
  id: number;
  title: string;
  leadId?: number | null;
  leadContactName?: string | null;
  leadSource?: string | null;
  contactId?: number | null;
  assignedUserId: number;
  assignedUserName?: string | null;
  assignedUserEmail?: string | null;
  status: ReminderStatus;
  dueDate?: string | null;
  notes?: string | null;
  overdue?: boolean;
  createdAt?: string;
  updatedAt?: string | null;
}

export interface ReminderRequest {
  title: string;
  dueDate?: string | null;
  leadId?: number | null;
  assignedUserId: number;
  status?: ReminderStatus;
  notes?: string | null;
}

