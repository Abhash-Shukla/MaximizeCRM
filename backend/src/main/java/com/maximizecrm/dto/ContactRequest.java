package com.maximizecrm.dto;

import com.maximizecrm.entity.ContactCategory;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ContactRequest(
    @NotBlank(message = "Contact name is required")
    @Size(max = 100, message = "Contact name must not exceed 100 characters")
    String name,

    @Email(message = "Please provide a valid email address")
    @Size(max = 150, message = "Email must not exceed 150 characters")
    String email,

    @Size(max = 30, message = "Phone must not exceed 30 characters")
    String phone,

    ContactCategory category,

    @Size(max = 100, message = "Company name must not exceed 100 characters")
    String company,

    String notes
) {
}

