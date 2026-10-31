package com.maximizecrm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maximizecrm.config.GlobalExceptionHandler;
import com.maximizecrm.dto.LoginRequest;
import com.maximizecrm.dto.LoginResponse;
import com.maximizecrm.entity.Role;
import com.maximizecrm.repository.UserRepository;
import com.maximizecrm.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @Mock
    private UserRepository userRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        AuthController authController = new AuthController(authService, userRepository);
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
    }

    @Test
    @DisplayName("POST /api/auth/login should return 200 and token on valid credentials")
    void login_ValidCredentials_Returns200() throws Exception {
        LoginRequest request = new LoginRequest("admin@maximizecrm.local", "Admin@123");
        LoginResponse response = new LoginResponse(
            "fake-jwt-token",
            1L,
            "System Administrator",
            "admin@maximizecrm.local",
            Role.ADMIN
        );

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value("fake-jwt-token"))
            .andExpect(jsonPath("$.email").value("admin@maximizecrm.local"))
            .andExpect(jsonPath("$.fullName").value("System Administrator"))
            .andExpect(jsonPath("$.role").value("ADMIN"))
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("POST /api/auth/login should return 401 when credentials are bad")
    void login_BadCredentials_Returns401() throws Exception {
        LoginRequest request = new LoginRequest("admin@maximizecrm.local", "WrongPassword");

        when(authService.login(any(LoginRequest.class)))
            .thenThrow(new BadCredentialsException("Invalid email or password"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    @DisplayName("POST /api/auth/login should return 400 when email or password is empty")
    void login_InvalidInput_Returns400() throws Exception {
        LoginRequest invalidRequest = new LoginRequest("", "");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.errors.email").exists())
            .andExpect(jsonPath("$.errors.password").exists());
    }
}

