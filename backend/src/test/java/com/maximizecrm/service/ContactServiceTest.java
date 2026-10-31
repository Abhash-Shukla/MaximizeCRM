package com.maximizecrm.service;

import com.maximizecrm.dto.ContactRequest;
import com.maximizecrm.dto.ContactResponse;
import com.maximizecrm.entity.Contact;
import com.maximizecrm.entity.ContactCategory;
import com.maximizecrm.exception.DuplicateResourceException;
import com.maximizecrm.exception.ResourceNotFoundException;
import com.maximizecrm.repository.ContactRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContactServiceTest {

    @Mock
    private ContactRepository contactRepository;

    @Mock
    private LeadValidator leadValidator;

    private ContactService contactService;

    @BeforeEach
    void setUp() {
        contactService = new ContactService(contactRepository, leadValidator);
    }

    @Test
    @DisplayName("createContact should succeed with valid data")
    void createContact_Success() {
        ContactRequest request = new ContactRequest(
            "Alice Smith",
            "alice@example.com",
            "+1-555-1234",
            ContactCategory.VIP,
            "Acme Corp",
            "Key decision maker"
        );

        Contact savedContact = new Contact(
            "Alice Smith",
            "alice@example.com",
            "+1-555-1234",
            ContactCategory.VIP,
            "Acme Corp",
            "Key decision maker"
        );
        savedContact.setId(1L);

        when(contactRepository.existsByEmailIgnoreCase("alice@example.com")).thenReturn(false);
        when(contactRepository.existsByPhone("+1-555-1234")).thenReturn(false);
        when(contactRepository.save(any(Contact.class))).thenReturn(savedContact);

        ContactResponse response = contactService.createContact(request);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("Alice Smith", response.name());
        assertEquals("alice@example.com", response.email());
        assertEquals("+1-555-1234", response.phone());
        assertEquals(ContactCategory.VIP, response.category());
        assertEquals("Acme Corp", response.company());
        verify(contactRepository).save(any(Contact.class));
    }

    @Test
    @DisplayName("createContact should reject duplicate email")
    void createContact_DuplicateEmail_ThrowsDuplicateResourceException() {
        ContactRequest request = new ContactRequest(
            "Alice Smith",
            "alice@example.com",
            "+1-555-1234",
            ContactCategory.NEW,
            null,
            null
        );

        when(contactRepository.existsByEmailIgnoreCase("alice@example.com")).thenReturn(true);

        DuplicateResourceException exception = assertThrows(
            DuplicateResourceException.class,
            () -> contactService.createContact(request)
        );

        assertTrue(exception.getMessage().contains("alice@example.com"));
        verify(contactRepository, never()).save(any());
    }

    @Test
    @DisplayName("createContact should reject duplicate phone")
    void createContact_DuplicatePhone_ThrowsDuplicateResourceException() {
        ContactRequest request = new ContactRequest(
            "Alice Smith",
            "alice@example.com",
            "+1-555-1234",
            ContactCategory.NEW,
            null,
            null
        );

        when(contactRepository.existsByEmailIgnoreCase("alice@example.com")).thenReturn(false);
        when(contactRepository.existsByPhone("+1-555-1234")).thenReturn(true);

        DuplicateResourceException exception = assertThrows(
            DuplicateResourceException.class,
            () -> contactService.createContact(request)
        );

        assertTrue(exception.getMessage().contains("+1-555-1234"));
        verify(contactRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateContact should succeed with valid data")
    void updateContact_Success() {
        Contact existing = new Contact(
            "Alice",
            "alice@example.com",
            "+1-555-1111",
            ContactCategory.NEW,
            "Old Corp",
            "Old note"
        );
        existing.setId(10L);

        ContactRequest updateRequest = new ContactRequest(
            "Alice Updated",
            "alice.updated@example.com",
            "+1-555-2222",
            ContactCategory.REGULAR,
            "New Corp",
            "Updated note"
        );

        when(contactRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(contactRepository.existsByEmailIgnoreCaseAndIdNot("alice.updated@example.com", 10L)).thenReturn(false);
        when(contactRepository.existsByPhoneAndIdNot("+1-555-2222", 10L)).thenReturn(false);
        when(contactRepository.save(existing)).thenReturn(existing);

        ContactResponse response = contactService.updateContact(10L, updateRequest);

        assertNotNull(response);
        assertEquals("Alice Updated", response.name());
        assertEquals("alice.updated@example.com", response.email());
        assertEquals("+1-555-2222", response.phone());
        assertEquals(ContactCategory.REGULAR, response.category());
        verify(contactRepository).save(existing);
    }

    @Test
    @DisplayName("updateContact should reject duplicate email from another contact")
    void updateContact_DuplicateEmail_ThrowsDuplicateResourceException() {
        Contact existing = new Contact("Bob", "bob@example.com", null, ContactCategory.NEW, null, null);
        existing.setId(2L);

        ContactRequest updateRequest = new ContactRequest(
            "Bob",
            "taken@example.com",
            null,
            ContactCategory.NEW,
            null,
            null
        );

        when(contactRepository.findById(2L)).thenReturn(Optional.of(existing));
        when(contactRepository.existsByEmailIgnoreCaseAndIdNot("taken@example.com", 2L)).thenReturn(true);

        DuplicateResourceException exception = assertThrows(
            DuplicateResourceException.class,
            () -> contactService.updateContact(2L, updateRequest)
        );

        assertTrue(exception.getMessage().contains("taken@example.com"));
        verify(contactRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateContact should reject duplicate phone from another contact")
    void updateContact_DuplicatePhone_ThrowsDuplicateResourceException() {
        Contact existing = new Contact("Bob", "bob@example.com", "+1-000-0000", ContactCategory.NEW, null, null);
        existing.setId(2L);

        ContactRequest updateRequest = new ContactRequest(
            "Bob",
            "bob@example.com",
            "+1-999-9999",
            ContactCategory.NEW,
            null,
            null
        );

        when(contactRepository.findById(2L)).thenReturn(Optional.of(existing));
        when(contactRepository.existsByEmailIgnoreCaseAndIdNot("bob@example.com", 2L)).thenReturn(false);
        when(contactRepository.existsByPhoneAndIdNot("+1-999-9999", 2L)).thenReturn(true);

        DuplicateResourceException exception = assertThrows(
            DuplicateResourceException.class,
            () -> contactService.updateContact(2L, updateRequest)
        );

        assertTrue(exception.getMessage().contains("+1-999-9999"));
        verify(contactRepository, never()).save(any());
    }

    @Test
    @DisplayName("deleteContact should reject deletion when contact has associated leads")
    void deleteContact_WithAssociatedLeads_ThrowsIllegalStateException() {
        Contact contact = new Contact("Charlie", "charlie@example.com", null, ContactCategory.NEW, null, null);
        contact.setId(5L);

        when(contactRepository.findById(5L)).thenReturn(Optional.of(contact));
        when(leadValidator.hasAssociatedLeads(5L)).thenReturn(true);

        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> contactService.deleteContact(5L)
        );

        assertEquals("Cannot delete contact: contact has associated leads.", exception.getMessage());
        verify(contactRepository, never()).delete(any());
    }

    @Test
    @DisplayName("deleteContact should succeed when contact has no associated leads")
    void deleteContact_WithoutAssociatedLeads_Success() {
        Contact contact = new Contact("Charlie", "charlie@example.com", null, ContactCategory.NEW, null, null);
        contact.setId(5L);

        when(contactRepository.findById(5L)).thenReturn(Optional.of(contact));
        when(leadValidator.hasAssociatedLeads(5L)).thenReturn(false);

        contactService.deleteContact(5L);

        verify(contactRepository).delete(contact);
    }

    @Test
    @DisplayName("getContactById should throw ResourceNotFoundException when contact does not exist")
    void getContactById_NotFound_ThrowsResourceNotFoundException() {
        when(contactRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(
            ResourceNotFoundException.class,
            () -> contactService.getContactById(999L)
        );
    }
}

