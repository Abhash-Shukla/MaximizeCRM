package com.maximizecrm.dto;

import com.maximizecrm.entity.Role;
import com.maximizecrm.entity.User;

import java.time.LocalDateTime;

public record UserResponse(
    Long id,
    String fullName,
    String email,
    Role role,
    boolean active,
    LocalDateTime createdAt
) {
    public static UserResponse fromEntity(User user) {
        if (user == null) {
            return null;
        }
        return new UserResponse(
            user.getId(),
            user.getFullName(),
            user.getEmail(),
            user.getRole(),
            user.isActive(),
            user.getCreatedAt()
        );
    }
}

