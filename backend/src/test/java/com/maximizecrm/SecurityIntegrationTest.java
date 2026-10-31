package com.maximizecrm;

import com.maximizecrm.entity.User;
import com.maximizecrm.repository.UserRepository;
import com.maximizecrm.service.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("GET /api/leads without token should return 401")
    void getLeads_WithoutToken_Returns401() throws Exception {
        mockMvc.perform(get("/api/leads"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/leads with token should return 200")
    void getLeads_WithToken_Returns200() throws Exception {
        User admin = userRepository.findByEmailIgnoreCase("admin@maximizecrm.local").orElseThrow();
        String token = jwtService.generateToken(admin);

        mockMvc.perform(get("/api/leads")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/contacts with token should return 200")
    void getContacts_WithToken_Returns200() throws Exception {
        User admin = userRepository.findByEmailIgnoreCase("admin@maximizecrm.local").orElseThrow();
        String token = jwtService.generateToken(admin);

        mockMvc.perform(get("/api/contacts")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/auth/me with token should return 200")
    void getMe_WithToken_Returns200() throws Exception {
        User admin = userRepository.findByEmailIgnoreCase("admin@maximizecrm.local").orElseThrow();
        String token = jwtService.generateToken(admin);

        mockMvc.perform(get("/api/auth/me")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk());
    }
}
