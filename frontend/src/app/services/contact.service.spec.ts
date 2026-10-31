import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ContactService } from './contact.service';
import { Contact, ContactRequest } from '../models/contact.models';

describe('ContactService', () => {
  let service: ContactService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.resetTestingModule();

    TestBed.configureTestingModule({
      providers: [
        ContactService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });

    service = TestBed.inject(ContactService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should fetch contacts with search and category filters', () => {
    const mockContacts: Contact[] = [
      {
        id: 1,
        name: 'Jane Doe',
        email: 'jane@example.com',
        phone: '+1-555-0100',
        category: 'VIP',
        company: 'Acme Corp'
      }
    ];

    service.getContacts('Jane', 'VIP').subscribe((contacts) => {
      expect(contacts.length).toBe(1);
      expect(contacts[0].name).toBe('Jane Doe');
      expect(contacts[0].category).toBe('VIP');
    });

    const req = httpMock.expectOne('/api/contacts?search=Jane&category=VIP');
    expect(req.request.method).toBe('GET');
    req.flush(mockContacts);
  });

  it('should create a new contact', () => {
    const request: ContactRequest = {
      name: 'Bob Ross',
      email: 'bob@example.com',
      phone: '+1-555-0200',
      category: 'NEW',
      company: 'Art Studio',
      notes: 'Creative director'
    };

    const mockResponse: Contact = {
      id: 2,
      ...request,
      createdAt: '2026-10-31T22:00:00'
    };

    service.createContact(request).subscribe((contact) => {
      expect(contact.id).toBe(2);
      expect(contact.name).toBe('Bob Ross');
    });

    const req = httpMock.expectOne('/api/contacts');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush(mockResponse);
  });

  it('should update an existing contact', () => {
    const updateRequest: ContactRequest = {
      name: 'Bob Ross Updated',
      email: 'bob.updated@example.com',
      phone: '+1-555-0200',
      category: 'REGULAR',
      company: 'Art Studio Inc',
      notes: 'Promoted'
    };

    const mockResponse: Contact = {
      id: 2,
      ...updateRequest,
      updatedAt: '2026-10-31T22:10:00'
    };

    service.updateContact(2, updateRequest).subscribe((contact) => {
      expect(contact.name).toBe('Bob Ross Updated');
      expect(contact.category).toBe('REGULAR');
    });

    const req = httpMock.expectOne('/api/contacts/2');
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(updateRequest);
    req.flush(mockResponse);
  });

  it('should delete a contact by id', () => {
    service.deleteContact(2).subscribe((res) => {
      expect(res.status).toBe(200);
      expect(res.message).toBe('Contact deleted successfully');
    });

    const req = httpMock.expectOne('/api/contacts/2');
    expect(req.request.method).toBe('DELETE');
    req.flush({ status: 200, message: 'Contact deleted successfully' });
  });

  it('should get contact by id', () => {
    const mockContact: Contact = {
      id: 5,
      name: 'Alice Wonder',
      category: 'NEW'
    };

    service.getContactById(5).subscribe((c) => {
      expect(c.id).toBe(5);
      expect(c.name).toBe('Alice Wonder');
    });

    const req = httpMock.expectOne('/api/contacts/5');
    expect(req.request.method).toBe('GET');
    req.flush(mockContact);
  });
});

