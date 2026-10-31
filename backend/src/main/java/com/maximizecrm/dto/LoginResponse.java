package com.maximizecrm.dto;

import com.maximizecrm.entity.Role;

public record LoginResponse(
    String token,
    Long id,
    String fullName,
    String email,
    Role role
) {
}

