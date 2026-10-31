package com.maximizecrm;

import com.maximizecrm.entity.User;
import com.maximizecrm.repository.UserRepository;
import com.maximizecrm.service.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    @Test
    @DisplayName("GET /api/deals without token should return 401")
    void getDeals_WithoutToken_Returns401() throws Exception {
        mockMvc.perform(get("/api/deals"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/deals with token should return 200")
    void getDeals_WithToken_Returns200() throws Exception {
        User admin = userRepository.findByEmailIgnoreCase("admin@maximizecrm.local").orElseThrow();
        String token = jwtService.generateToken(admin);

        mockMvc.perform(get("/api/deals")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/campaigns without token should return 401")
    void getCampaigns_WithoutToken_Returns401() throws Exception {
        mockMvc.perform(get("/api/campaigns"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/campaigns with token should return 200")
    void getCampaigns_WithToken_Returns200() throws Exception {
        User admin = userRepository.findByEmailIgnoreCase("admin@maximizecrm.local").orElseThrow();
        String token = jwtService.generateToken(admin);

        mockMvc.perform(get("/api/campaigns")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/reminders without token should return 401")
    void getReminders_WithoutToken_Returns401() throws Exception {
        mockMvc.perform(get("/api/reminders"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/reminders with token should return 200")
    void getReminders_WithToken_Returns200() throws Exception {
        User admin = userRepository.findByEmailIgnoreCase("admin@maximizecrm.local").orElseThrow();
        String token = jwtService.generateToken(admin);

        mockMvc.perform(get("/api/reminders")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/reports/summary without token should return 401")
    void getReportsSummary_WithoutToken_Returns401() throws Exception {
        mockMvc.perform(get("/api/reports/summary"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/reports/summary with token should return 200")
    void getReportsSummary_WithToken_Returns200() throws Exception {
        User admin = userRepository.findByEmailIgnoreCase("admin@maximizecrm.local").orElseThrow();
        String token = jwtService.generateToken(admin);

        mockMvc.perform(get("/api/reports/summary")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/admin/users without token should return 401")
    void getAdminUsers_WithoutToken_Returns401() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/admin/users with SALES_REP token should return 403 Forbidden (FR-8.6 / AT-11)")
    void getAdminUsers_WithSalesRepToken_Returns403() throws Exception {
        User rep = userRepository.findByEmailIgnoreCase("rep@maximizecrm.local").orElseThrow();
        String token = jwtService.generateToken(rep);

        mockMvc.perform(get("/api/admin/users")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/admin/users with ADMIN token should return 200 OK")
    void getAdminUsers_WithAdminToken_Returns200() throws Exception {
        User admin = userRepository.findByEmailIgnoreCase("admin@maximizecrm.local").orElseThrow();
        String token = jwtService.generateToken(admin);

        mockMvc.perform(get("/api/admin/users")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/users with SALES_REP token should return 403 Forbidden")
    void createUser_WithSalesRepToken_Returns403() throws Exception {
        User rep = userRepository.findByEmailIgnoreCase("rep@maximizecrm.local").orElseThrow();
        String token = jwtService.generateToken(rep);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"fullName\":\"Test\",\"email\":\"test@demo.com\",\"password\":\"Pass@123\",\"role\":\"SALES_REP\"}")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/users (assignable) with SALES_REP token should return 200 OK")
    void getAssignableUsers_WithSalesRepToken_Returns200() throws Exception {
        User rep = userRepository.findByEmailIgnoreCase("rep@maximizecrm.local").orElseThrow();
        String token = jwtService.generateToken(rep);

        mockMvc.perform(get("/api/users")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk());
    }
}
