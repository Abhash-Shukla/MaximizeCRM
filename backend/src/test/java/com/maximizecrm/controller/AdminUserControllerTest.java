package com.maximizecrm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maximizecrm.config.GlobalExceptionHandler;
import com.maximizecrm.dto.CreateUserRequest;
import com.maximizecrm.dto.UpdateUserRequest;
import com.maximizecrm.dto.UserResponse;
import com.maximizecrm.entity.Role;
import com.maximizecrm.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminUserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        AdminUserController controller = new AdminUserController(userService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
    }

    @Test
    @DisplayName("GET /api/admin/users should return 200 with user list")
    void getAllUsers_Returns200() throws Exception {
        UserResponse user = new UserResponse(1L, "Admin User", "admin@maximizecrm.local", Role.ADMIN, true, LocalDateTime.now());

        when(userService.getUsers(any(), any(), any())).thenReturn(List.of(user));

        mockMvc.perform(get("/api/admin/users"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].email").value("admin@maximizecrm.local"))
            .andExpect(jsonPath("$[0].role").value("ADMIN"));
    }

    @Test
    @DisplayName("POST /api/admin/users should return 201 with created user")
    void createUser_Returns201() throws Exception {
        CreateUserRequest request = new CreateUserRequest("New Rep", "newrep@maximizecrm.local", "Password@123", Role.SALES_REP, true);
        UserResponse created = new UserResponse(5L, request.fullName(), request.email(), request.role(), true, LocalDateTime.now());

        when(userService.createUser(any(CreateUserRequest.class))).thenReturn(created);

        mockMvc.perform(post("/api/admin/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(5))
            .andExpect(jsonPath("$.fullName").value("New Rep"));
    }

    @Test
    @DisplayName("PUT /api/admin/users/{id} should return 200 with updated user")
    void updateUser_Returns200() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest("Updated Name", "updated@maximizecrm.local", null, Role.SALES_REP, true);
        UserResponse updated = new UserResponse(5L, request.fullName(), request.email(), request.role(), true, LocalDateTime.now());

        when(userService.updateUser(eq(5L), any(UpdateUserRequest.class))).thenReturn(updated);

        mockMvc.perform(put("/api/admin/users/5")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.fullName").value("Updated Name"));
    }

    @Test
    @DisplayName("PATCH /api/admin/users/{id}/deactivate should return 200 with active=false")
    void deactivateUser_Returns200() throws Exception {
        UserResponse deactivated = new UserResponse(5L, "New Rep", "newrep@maximizecrm.local", Role.SALES_REP, false, LocalDateTime.now());

        when(userService.setUserActive(5L, false)).thenReturn(deactivated);

        mockMvc.perform(patch("/api/admin/users/5/deactivate"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.active").value(false));
    }
}

