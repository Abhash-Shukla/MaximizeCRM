package com.maximizecrm.dto;

import com.maximizecrm.entity.Role;
import com.maximizecrm.entity.User;

public record UserSummaryResponse(
    Long id,
    String fullName,
    String email,
    Role role,
    boolean active
) {
    public static UserSummaryResponse fromEntity(User user) {
        if (user == null) {
            return null;
        }
        return new UserSummaryResponse(
            user.getId(),
            user.getFullName(),
            user.getEmail(),
            user.getRole(),
            user.isActive()
        );
    }
}

