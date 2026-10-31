package com.maximizecrm.dto;

import com.maximizecrm.entity.Contact;
import com.maximizecrm.entity.ContactCategory;

import java.time.LocalDateTime;

public record ContactResponse(
    Long id,
    String name,
    String email,
    String phone,
    ContactCategory category,
    String company,
    String notes,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static ContactResponse fromEntity(Contact contact) {
        if (contact == null) {
            return null;
        }
        return new ContactResponse(
            contact.getId(),
            contact.getName(),
            contact.getEmail(),
            contact.getPhone(),
            contact.getCategory(),
            contact.getCompany(),
            contact.getNotes(),
            contact.getCreatedAt(),
            contact.getUpdatedAt()
        );
    }
}

