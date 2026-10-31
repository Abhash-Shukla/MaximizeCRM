package com.maximizecrm.service;

import com.maximizecrm.dto.LoginRequest;
import com.maximizecrm.dto.LoginResponse;
import com.maximizecrm.entity.Role;
import com.maximizecrm.entity.User;
import com.maximizecrm.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setFullName("Test Admin");
        testUser.setEmail("admin@maximizecrm.local");
        testUser.setPasswordHash("$2a$10$hashedPasswordHere");
        testUser.setRole(Role.ADMIN);
        testUser.setActive(true);
    }

    @Test
    @DisplayName("login() should succeed and return JWT token when credentials are valid")
    void login_Success() {
        LoginRequest request = new LoginRequest("admin@maximizecrm.local", "Admin@123");

        when(userRepository.findByEmailIgnoreCase("admin@maximizecrm.local")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("Admin@123", testUser.getPasswordHash())).thenReturn(true);
        when(jwtService.generateToken(testUser)).thenReturn("mocked-jwt-token");

        LoginResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("mocked-jwt-token", response.token());
        assertEquals("admin@maximizecrm.local", response.email());
        assertEquals("Test Admin", response.fullName());
        assertEquals(Role.ADMIN, response.role());
        assertEquals(1L, response.id());
    }

    @Test
    @DisplayName("login() should throw BadCredentialsException when password does not match")
    void login_InvalidPassword() {
        LoginRequest request = new LoginRequest("admin@maximizecrm.local", "WrongPassword");

        when(userRepository.findByEmailIgnoreCase("admin@maximizecrm.local")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("WrongPassword", testUser.getPasswordHash())).thenReturn(false);

        BadCredentialsException exception = assertThrows(
            BadCredentialsException.class,
            () -> authService.login(request)
        );

        assertEquals("Invalid email or password", exception.getMessage());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    @DisplayName("login() should throw BadCredentialsException when email is not found")
    void login_EmailNotFound() {
        LoginRequest request = new LoginRequest("unknown@maximizecrm.local", "Admin@123");

        when(userRepository.findByEmailIgnoreCase("unknown@maximizecrm.local")).thenReturn(Optional.empty());

        BadCredentialsException exception = assertThrows(
            BadCredentialsException.class,
            () -> authService.login(request)
        );

        assertEquals("Invalid email or password", exception.getMessage());
    }

    @Test
    @DisplayName("login() should throw DisabledException when user account is inactive")
    void login_InactiveAccount() {
        testUser.setActive(false);
        LoginRequest request = new LoginRequest("admin@maximizecrm.local", "Admin@123");

        when(userRepository.findByEmailIgnoreCase("admin@maximizecrm.local")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("Admin@123", testUser.getPasswordHash())).thenReturn(true);

        assertThrows(DisabledException.class, () -> authService.login(request));
    }
}

