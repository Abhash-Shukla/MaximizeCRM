package com.maximizecrm.service;

import com.maximizecrm.dto.CreateUserRequest;
import com.maximizecrm.dto.UpdateUserRequest;
import com.maximizecrm.dto.UserResponse;
import com.maximizecrm.dto.UserSummaryResponse;
import com.maximizecrm.entity.Role;
import com.maximizecrm.entity.User;
import com.maximizecrm.exception.DuplicateResourceException;
import com.maximizecrm.exception.ResourceNotFoundException;
import com.maximizecrm.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getUsers(String search, Role role, Boolean active) {
        String trimmedSearch = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
        List<User> users = userRepository.searchUsers(trimmedSearch, role, active);
        return users.stream().map(UserResponse::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public List<UserSummaryResponse> getAssignableUsers() {
        return userRepository.findAll().stream()
            .filter(User::isActive)
            .map(UserSummaryResponse::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return UserResponse.fromEntity(user);
    }

    public UserResponse createUser(CreateUserRequest request) {
        String cleanEmail = request.email().trim().toLowerCase();

        if (userRepository.existsByEmailIgnoreCase(cleanEmail)) {
            throw new DuplicateResourceException("Email is already registered: " + cleanEmail);
        }

        String passwordHash = passwordEncoder.encode(request.password());
        boolean active = request.active() != null ? request.active() : true;

        User user = new User(
            request.fullName().trim(),
            cleanEmail,
            passwordHash,
            request.role(),
            active
        );

        User saved = userRepository.save(user);
        return UserResponse.fromEntity(saved);
    }

    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        String cleanEmail = request.email().trim().toLowerCase();

        if (userRepository.existsByEmailIgnoreCaseAndIdNot(cleanEmail, id)) {
            throw new DuplicateResourceException("Email is already in use by another user: " + cleanEmail);
        }

        // Validate deactivation safety rule
        if (request.active() != null && !request.active() && user.isActive() && user.getRole() == Role.ADMIN) {
            validateNotLastActiveAdmin(id);
        }

        user.setFullName(request.fullName().trim());
        user.setEmail(cleanEmail);
        user.setRole(request.role());

        if (request.active() != null) {
            user.setActive(request.active());
        }

        if (request.password() != null && !request.password().trim().isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(request.password().trim()));
        }

        User updated = userRepository.save(user);
        return UserResponse.fromEntity(updated);
    }

    public UserResponse setUserActive(Long id, boolean active) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (!active && user.isActive() && user.getRole() == Role.ADMIN) {
            validateNotLastActiveAdmin(id);
        }

        user.setActive(active);
        User updated = userRepository.save(user);
        return UserResponse.fromEntity(updated);
    }

    private void validateNotLastActiveAdmin(Long targetUserId) {
        long activeAdminCount = userRepository.findAll().stream()
            .filter(u -> u.getRole() == Role.ADMIN && u.isActive())
            .count();

        if (activeAdminCount <= 1) {
            throw new IllegalStateException("Cannot deactivate the only remaining active administrator.");
        }
    }
}

