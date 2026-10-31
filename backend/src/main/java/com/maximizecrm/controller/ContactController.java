package com.maximizecrm.controller;

import com.maximizecrm.dto.ContactRequest;
import com.maximizecrm.dto.ContactResponse;
import com.maximizecrm.entity.ContactCategory;
import com.maximizecrm.service.ContactService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/contacts")
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @PostMapping
    public ResponseEntity<ContactResponse> createContact(@Valid @RequestBody ContactRequest request) {
        ContactResponse created = contactService.createContact(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<ContactResponse>> getContacts(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) ContactCategory category
    ) {
        List<ContactResponse> contacts = contactService.getContacts(search, category);
        return ResponseEntity.ok(contacts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContactResponse> getContactById(@PathVariable Long id) {
        ContactResponse contact = contactService.getContactById(id);
        return ResponseEntity.ok(contact);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ContactResponse> updateContact(
        @PathVariable Long id,
        @Valid @RequestBody ContactRequest request
    ) {
        ContactResponse updated = contactService.updateContact(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteContact(@PathVariable Long id) {
        contactService.deleteContact(id);
        return ResponseEntity.ok(Map.of(
            "status", HttpStatus.OK.value(),
            "message", "Contact deleted successfully"
        ));
    }
}

