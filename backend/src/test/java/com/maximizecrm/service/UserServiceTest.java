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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;
    private User sampleUser;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, passwordEncoder);

        sampleUser = new User("Alice Rep", "alice@maximizecrm.local", "hashedPassword", Role.SALES_REP, true);
        sampleUser.setId(10L);
    }

    @Test
    @DisplayName("getUsers should query repository with filters and return response DTOs")
    void getUsers_WithFilters_ReturnsList() {
        when(userRepository.searchUsers("Alice", Role.SALES_REP, true))
            .thenReturn(List.of(sampleUser));

        List<UserResponse> result = userService.getUsers("Alice", Role.SALES_REP, true);

        assertEquals(1, result.size());
        assertEquals("Alice Rep", result.get(0).fullName());
        assertEquals("alice@maximizecrm.local", result.get(0).email());
        assertEquals(Role.SALES_REP, result.get(0).role());
        assertTrue(result.get(0).active());
    }

    @Test
    @DisplayName("getAssignableUsers should return active users only")
    void getAssignableUsers_ReturnsActiveUsers() {
        User inactive = new User("Old Rep", "old@maximizecrm.local", "hash", Role.SALES_REP, false);
        inactive.setId(11L);

        when(userRepository.findAll()).thenReturn(List.of(sampleUser, inactive));

        List<UserSummaryResponse> result = userService.getAssignableUsers();

        assertEquals(1, result.size());
        assertEquals("Alice Rep", result.get(0).fullName());
    }

    @Test
    @DisplayName("getUserById should return user when found")
    void getUserById_Success() {
        when(userRepository.findById(10L)).thenReturn(Optional.of(sampleUser));

        UserResponse result = userService.getUserById(10L);

        assertNotNull(result);
        assertEquals(10L, result.id());
        assertEquals("Alice Rep", result.fullName());
    }

    @Test
    @DisplayName("createUser should hash password and save user")
    void createUser_Success() {
        CreateUserRequest request = new CreateUserRequest(
            "Bob Admin",
            "bob@maximizecrm.local",
            "Secret@123",
            Role.ADMIN,
            true
        );

        when(userRepository.existsByEmailIgnoreCase("bob@maximizecrm.local")).thenReturn(false);
        when(passwordEncoder.encode("Secret@123")).thenReturn("encodedSecret");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(20L);
            return u;
        });

        UserResponse result = userService.createUser(request);

        assertNotNull(result);
        assertEquals(20L, result.id());
        assertEquals("Bob Admin", result.fullName());
        assertEquals("bob@maximizecrm.local", result.email());
        assertEquals(Role.ADMIN, result.role());
        assertTrue(result.active());
        verify(passwordEncoder).encode("Secret@123");
    }

    @Test
    @DisplayName("createUser should throw DuplicateResourceException when email already exists")
    void createUser_DuplicateEmail_ThrowsException() {
        CreateUserRequest request = new CreateUserRequest(
            "Bob Admin",
            "existing@maximizecrm.local",
            "Secret@123",
            Role.ADMIN,
            true
        );

        when(userRepository.existsByEmailIgnoreCase("existing@maximizecrm.local")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> userService.createUser(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateUser should update fields and optional new password")
    void updateUser_Success() {
        UpdateUserRequest request = new UpdateUserRequest(
            "Alice Senior Rep",
            "alice.sr@maximizecrm.local",
            "NewPassword@123",
            Role.SALES_REP,
            true
        );

        when(userRepository.findById(10L)).thenReturn(Optional.of(sampleUser));
        when(userRepository.existsByEmailIgnoreCaseAndIdNot("alice.sr@maximizecrm.local", 10L)).thenReturn(false);
        when(passwordEncoder.encode("NewPassword@123")).thenReturn("encodedNewPassword");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponse result = userService.updateUser(10L, request);

        assertEquals("Alice Senior Rep", result.fullName());
        assertEquals("alice.sr@maximizecrm.local", result.email());
        verify(passwordEncoder).encode("NewPassword@123");
    }

    @Test
    @DisplayName("setUserActive should update active status")
    void setUserActive_Success() {
        when(userRepository.findById(10L)).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponse result = userService.setUserActive(10L, false);

        assertFalse(result.active());
    }

    @Test
    @DisplayName("setUserActive should throw IllegalStateException when attempting to deactivate last active admin")
    void setUserActive_DeactivateLastAdmin_ThrowsException() {
        User admin = new User("Chief Admin", "admin@maximizecrm.local", "hash", Role.ADMIN, true);
        admin.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(userRepository.findAll()).thenReturn(List.of(admin)); // Only 1 active admin

        IllegalStateException ex = assertThrows(IllegalStateException.class,
            () -> userService.setUserActive(1L, false));
        assertTrue(ex.getMessage().contains("Cannot deactivate the only remaining active administrator"));
    }
}

