import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ReminderService } from './reminder.service';
import { Reminder, ReminderRequest } from '../models/reminder.models';

describe('ReminderService', () => {
  let service: ReminderService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.resetTestingModule();

    TestBed.configureTestingModule({
      providers: [
        ReminderService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });

    service = TestBed.inject(ReminderService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should fetch reminders with search, status, and assignedUserId parameters', () => {
    const mockReminders: Reminder[] = [
      {
        id: 1,
        title: 'Follow-up with Acme',
        status: 'PENDING',
        assignedUserId: 20,
        assignedUserName: 'Sarah Rep',
        leadId: 10,
        leadContactName: 'Alice Buyer',
        dueDate: '2026-06-15T10:00:00',
        overdue: false,
        createdAt: '2026-06-01T10:00:00'
      }
    ];

    service.getReminders('Acme', 'PENDING', 20).subscribe((reminders) => {
      expect(reminders.length).toBe(1);
      expect(reminders[0].title).toBe('Follow-up with Acme');
      expect(reminders[0].status).toBe('PENDING');
      expect(reminders[0].assignedUserId).toBe(20);
    });

    const req = httpMock.expectOne('/api/reminders?search=Acme&status=PENDING&assignedUserId=20');
    expect(req.request.method).toBe('GET');
    req.flush(mockReminders);
  });

  it('should fetch a single reminder by ID', () => {
    const mockReminder: Reminder = {
      id: 5,
      title: 'Demo presentation',
      status: 'PENDING',
      assignedUserId: 20,
      assignedUserName: 'Sarah Rep'
    };

    service.getReminderById(5).subscribe((reminder) => {
      expect(reminder.id).toBe(5);
      expect(reminder.title).toBe('Demo presentation');
    });

    const req = httpMock.expectOne('/api/reminders/5');
    expect(req.request.method).toBe('GET');
    req.flush(mockReminder);
  });

  it('should create a new reminder', () => {
    const request: ReminderRequest = {
      title: 'Send contract draft',
      dueDate: '2026-06-20T14:00:00',
      leadId: 10,
      assignedUserId: 20,
      status: 'PENDING',
      notes: 'Ensure SLA appendix is included'
    };

    const mockResponse: Reminder = {
      id: 8,
      title: request.title,
      dueDate: request.dueDate,
      leadId: request.leadId,
      assignedUserId: request.assignedUserId,
      status: 'PENDING',
      notes: request.notes,
      assignedUserName: 'Sarah Rep',
      overdue: false,
      createdAt: '2026-06-01T10:00:00'
    };

    service.createReminder(request).subscribe((reminder) => {
      expect(reminder.id).toBe(8);
      expect(reminder.title).toBe('Send contract draft');
      expect(reminder.status).toBe('PENDING');
    });

    const req = httpMock.expectOne('/api/reminders');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush(mockResponse);
  });

  it('should update an existing reminder', () => {
    const updateRequest: ReminderRequest = {
      title: 'Send updated contract draft',
      dueDate: '2026-06-22T16:00:00',
      leadId: 10,
      assignedUserId: 20,
      status: 'PENDING',
      notes: 'Revised payment terms'
    };

    const mockResponse: Reminder = {
      id: 8,
      title: updateRequest.title,
      dueDate: updateRequest.dueDate,
      leadId: updateRequest.leadId,
      assignedUserId: updateRequest.assignedUserId,
      status: 'PENDING',
      notes: updateRequest.notes,
      assignedUserName: 'Sarah Rep'
    };

    service.updateReminder(8, updateRequest).subscribe((reminder) => {
      expect(reminder.id).toBe(8);
      expect(reminder.title).toBe('Send updated contract draft');
    });

    const req = httpMock.expectOne('/api/reminders/8');
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(updateRequest);
    req.flush(mockResponse);
  });

  it('should mark a reminder as completed', () => {
    const mockCompleted: Reminder = {
      id: 8,
      title: 'Send updated contract draft',
      assignedUserId: 20,
      status: 'COMPLETED'
    };

    service.completeReminder(8).subscribe((reminder) => {
      expect(reminder.id).toBe(8);
      expect(reminder.status).toBe('COMPLETED');
    });

    const req = httpMock.expectOne('/api/reminders/8/complete');
    expect(req.request.method).toBe('PATCH');
    req.flush(mockCompleted);
  });

  it('should delete a reminder', () => {
    const mockDeleteResponse = {
      status: 200,
      message: 'Reminder deleted successfully.'
    };

    service.deleteReminder(8).subscribe((res) => {
      expect(res.status).toBe(200);
      expect(res.message).toBe('Reminder deleted successfully.');
    });

    const req = httpMock.expectOne('/api/reminders/8');
    expect(req.request.method).toBe('DELETE');
    req.flush(mockDeleteResponse);
  });
});

