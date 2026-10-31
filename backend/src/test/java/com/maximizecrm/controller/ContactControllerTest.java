package com.maximizecrm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.maximizecrm.config.GlobalExceptionHandler;
import com.maximizecrm.dto.ContactRequest;
import com.maximizecrm.dto.ContactResponse;
import com.maximizecrm.entity.ContactCategory;
import com.maximizecrm.exception.DuplicateResourceException;
import com.maximizecrm.exception.ResourceNotFoundException;
import com.maximizecrm.service.ContactService;
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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ContactControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ContactService contactService;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @BeforeEach
    void setUp() {
        ContactController contactController = new ContactController(contactService);
        mockMvc = MockMvcBuilders.standaloneSetup(contactController)
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
    }

    @Test
    @DisplayName("POST /api/contacts returns 201 Created on valid input")
    void createContact_ValidInput_Returns201() throws Exception {
        ContactRequest request = new ContactRequest(
            "Jane Doe",
            "jane@example.com",
            "+1-555-8888",
            ContactCategory.NEW,
            "Tech Inc",
            "Initial inquiry"
        );

        ContactResponse response = new ContactResponse(
            1L,
            "Jane Doe",
            "jane@example.com",
            "+1-555-8888",
            ContactCategory.NEW,
            "Tech Inc",
            "Initial inquiry",
            LocalDateTime.now(),
            LocalDateTime.now()
        );

        when(contactService.createContact(any(ContactRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/contacts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Jane Doe"))
            .andExpect(jsonPath("$.email").value("jane@example.com"))
            .andExpect(jsonPath("$.category").value("NEW"));
    }

    @Test
    @DisplayName("POST /api/contacts returns 400 Bad Request when name is blank")
    void createContact_BlankName_Returns400() throws Exception {
        ContactRequest request = new ContactRequest(
            "",
            "jane@example.com",
            null,
            ContactCategory.NEW,
            null,
            null
        );

        mockMvc.perform(post("/api/contacts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.errors.name").exists());
    }

    @Test
    @DisplayName("POST /api/contacts returns 409 Conflict when duplicate email or phone")
    void createContact_Duplicate_Returns409() throws Exception {
        ContactRequest request = new ContactRequest(
            "Jane Doe",
            "existing@example.com",
            null,
            ContactCategory.NEW,
            null,
            null
        );

        when(contactService.createContact(any(ContactRequest.class)))
            .thenThrow(new DuplicateResourceException("A contact with email 'existing@example.com' already exists."));

        mockMvc.perform(post("/api/contacts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.message").value("A contact with email 'existing@example.com' already exists."));
    }

    @Test
    @DisplayName("GET /api/contacts returns 200 and list")
    void getContacts_Returns200() throws Exception {
        ContactResponse c1 = new ContactResponse(
            1L, "Alice", "alice@example.com", null, ContactCategory.NEW, null, null, null, null
        );
        ContactResponse c2 = new ContactResponse(
            2L, "Bob", "bob@example.com", null, ContactCategory.VIP, null, null, null, null
        );

        when(contactService.getContacts(null, null)).thenReturn(List.of(c1, c2));

        mockMvc.perform(get("/api/contacts"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].name").value("Alice"))
            .andExpect(jsonPath("$[1].name").value("Bob"));
    }

    @Test
    @DisplayName("GET /api/contacts/{id} returns 200 when found")
    void getContactById_Found_Returns200() throws Exception {
        ContactResponse response = new ContactResponse(
            5L, "Charlie", "charlie@example.com", null, ContactCategory.REGULAR, null, null, null, null
        );

        when(contactService.getContactById(5L)).thenReturn(response);

        mockMvc.perform(get("/api/contacts/5"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(5))
            .andExpect(jsonPath("$.name").value("Charlie"));
    }

    @Test
    @DisplayName("GET /api/contacts/{id} returns 404 when not found")
    void getContactById_NotFound_Returns404() throws Exception {
        when(contactService.getContactById(99L))
            .thenThrow(new ResourceNotFoundException("Contact not found with ID: 99"));

        mockMvc.perform(get("/api/contacts/99"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.message").value("Contact not found with ID: 99"));
    }

    @Test
    @DisplayName("PUT /api/contacts/{id} returns 200 on success")
    void updateContact_Success_Returns200() throws Exception {
        ContactRequest request = new ContactRequest(
            "Updated Name",
            "updated@example.com",
            null,
            ContactCategory.VIP,
            null,
            null
        );

        ContactResponse response = new ContactResponse(
            1L,
            "Updated Name",
            "updated@example.com",
            null,
            ContactCategory.VIP,
            null,
            null,
            null,
            null
        );

        when(contactService.updateContact(eq(1L), any(ContactRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/contacts/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Updated Name"))
            .andExpect(jsonPath("$.category").value("VIP"));
    }

    @Test
    @DisplayName("DELETE /api/contacts/{id} returns 200 on success")
    void deleteContact_Success_Returns200() throws Exception {
        doNothing().when(contactService).deleteContact(1L);

        mockMvc.perform(delete("/api/contacts/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.message").value("Contact deleted successfully"));

        verify(contactService).deleteContact(1L);
    }

    @Test
    @DisplayName("DELETE /api/contacts/{id} returns 409 Conflict when associated leads exist")
    void deleteContact_HasLeads_Returns409() throws Exception {
        doThrow(new IllegalStateException("Cannot delete contact: contact has associated leads."))
            .when(contactService).deleteContact(1L);

        mockMvc.perform(delete("/api/contacts/1"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.message").value("Cannot delete contact: contact has associated leads."));
    }
}

