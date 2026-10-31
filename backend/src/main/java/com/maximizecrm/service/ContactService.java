package com.maximizecrm.service;

import com.maximizecrm.dto.ContactRequest;
import com.maximizecrm.dto.ContactResponse;
import com.maximizecrm.entity.Contact;
import com.maximizecrm.entity.ContactCategory;
import com.maximizecrm.exception.DuplicateResourceException;
import com.maximizecrm.exception.ResourceNotFoundException;
import com.maximizecrm.repository.ContactRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ContactService {

    private final ContactRepository contactRepository;
    private final LeadValidator leadValidator;

    public ContactService(ContactRepository contactRepository, LeadValidator leadValidator) {
        this.contactRepository = contactRepository;
        this.leadValidator = leadValidator;
    }

    public ContactResponse createContact(ContactRequest request) {
        String cleanEmail = normalizeString(request.email());
        String cleanPhone = normalizeString(request.phone());
        String cleanName = request.name() != null ? request.name().trim() : null;
        String cleanCompany = normalizeString(request.company());
        String cleanNotes = normalizeString(request.notes());

        validateDuplicateEmailOnCreate(cleanEmail);
        validateDuplicatePhoneOnCreate(cleanPhone);

        ContactCategory category = request.category() != null ? request.category() : ContactCategory.NEW;

        Contact contact = new Contact(
            cleanName,
            cleanEmail,
            cleanPhone,
            category,
            cleanCompany,
            cleanNotes
        );

        Contact saved = contactRepository.save(contact);
        return ContactResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<ContactResponse> getContacts(String search, ContactCategory category) {
        String cleanSearch = normalizeString(search);
        List<Contact> contacts = contactRepository.searchContacts(cleanSearch, category);
        return contacts.stream()
            .map(ContactResponse::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public ContactResponse getContactById(Long id) {
        Contact contact = contactRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Contact not found with ID: " + id));
        return ContactResponse.fromEntity(contact);
    }

    public ContactResponse updateContact(Long id, ContactRequest request) {
        Contact contact = contactRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Contact not found with ID: " + id));

        String cleanEmail = normalizeString(request.email());
        String cleanPhone = normalizeString(request.phone());
        String cleanName = request.name() != null ? request.name().trim() : null;
        String cleanCompany = normalizeString(request.company());
        String cleanNotes = normalizeString(request.notes());

        validateDuplicateEmailOnUpdate(cleanEmail, id);
        validateDuplicatePhoneOnUpdate(cleanPhone, id);

        contact.setName(cleanName);
        contact.setEmail(cleanEmail);
        contact.setPhone(cleanPhone);
        if (request.category() != null) {
            contact.setCategory(request.category());
        }
        contact.setCompany(cleanCompany);
        contact.setNotes(cleanNotes);

        Contact updated = contactRepository.save(contact);
        return ContactResponse.fromEntity(updated);
    }

    public void deleteContact(Long id) {
        Contact contact = contactRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Contact not found with ID: " + id));

        if (leadValidator.hasAssociatedLeads(id)) {
            throw new IllegalStateException("Cannot delete contact: contact has associated leads.");
        }

        contactRepository.delete(contact);
    }

    private void validateDuplicateEmailOnCreate(String email) {
        if (email != null && contactRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateResourceException("A contact with email '" + email + "' already exists.");
        }
    }

    private void validateDuplicatePhoneOnCreate(String phone) {
        if (phone != null && contactRepository.existsByPhone(phone)) {
            throw new DuplicateResourceException("A contact with phone number '" + phone + "' already exists.");
        }
    }

    private void validateDuplicateEmailOnUpdate(String email, Long id) {
        if (email != null && contactRepository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new DuplicateResourceException("A contact with email '" + email + "' already exists.");
        }
    }

    private void validateDuplicatePhoneOnUpdate(String phone, Long id) {
        if (phone != null && contactRepository.existsByPhoneAndIdNot(phone, id)) {
            throw new DuplicateResourceException("A contact with phone number '" + phone + "' already exists.");
        }
    }

    private String normalizeString(String val) {
        if (val == null) {
            return null;
        }
        String trimmed = val.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}

