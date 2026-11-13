package com.example.bankingapi.controllers;

import com.example.bankingapi.config.SecurityConfig;
import com.example.bankingapi.dto.client.ClientResponse;
import com.example.bankingapi.exceptions.ClientNotFoundException;
import com.example.bankingapi.security.CustomUserDetailsService;
import com.example.bankingapi.security.JwtService;
import com.example.bankingapi.services.ClientService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClientController.class)
@Import(SecurityConfig.class)
class ClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClientService clientService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @Test
    @DisplayName("GET /api/clients: should return list of clients for authenticated user")
    @WithMockUser(roles = "USER")
    void getAllClients_shouldReturnList() throws Exception {
        ClientResponse response1 = new ClientResponse();
        response1.setClientId(1L);
        response1.setFullName("Ivan Ivanov");

        ClientResponse response2 = new ClientResponse();
        response2.setClientId(2L);
        response2.setFullName("Petr Petrov");

        when(clientService.getAllClients()).thenReturn(List.of(response1, response2));

        mockMvc.perform(get("/api/clients")).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(jsonPath("$.length()").value(2)).andExpect(jsonPath("$[0].clientId").value(1L)).andExpect(jsonPath("$[0].fullName").value("Ivan Ivanov")).andExpect(jsonPath("$[1].clientId").value(2L)).andExpect(jsonPath("$[1].fullName").value("Petr Petrov"));

        verify(clientService, times(1)).getAllClients();
    }

    @Test
    @DisplayName("GET /api/clients/{id}: should return client for authenticated user")
    @WithMockUser(roles = "USER")
    void getClientById_shouldReturnClient() throws Exception {
        ClientResponse response = new ClientResponse();
        response.setClientId(1L);
        response.setFirstName("Ivan");
        response.setLastName("Ivanov");
        response.setFullName("Ivan Ivanov");

        when(clientService.getClientById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/clients/1")).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(jsonPath("$.clientId").value(1L)).andExpect(jsonPath("$.firstName").value("Ivan")).andExpect(jsonPath("$.lastName").value("Ivanov")).andExpect(jsonPath("$.fullName").value("Ivan Ivanov"));

        verify(clientService, times(1)).getClientById(1L);
    }

    @Test
    @DisplayName("GET /api/clients/{id}: should return 401 for anonymous user")
    void getClientById_shouldReturnUnauthorizedForAnonymous() throws Exception {
        mockMvc.perform(get("/api/clients/1")).andExpect(status().isUnauthorized());

        verify(clientService, never()).getClientById(anyLong());
    }

    @Test
    @DisplayName("GET /api/clients/{id}: should return 404 when client not found")
    @WithMockUser(roles = "USER")
    void getClientById_shouldReturnNotFound() throws Exception {
        when(clientService.getClientById(999L)).thenThrow(new ClientNotFoundException(999L));

        mockMvc.perform(get("/api/clients/999")).andExpect(status().isNotFound()).andExpect(jsonPath("$.status").value(404)).andExpect(jsonPath("$.message").value(containsString("999")));

        verify(clientService, times(1)).getClientById(999L);
    }

    @Test
    @DisplayName("POST /api/clients: should return 201 with valid request")
    @WithMockUser(roles = "USER")
    void createClient_shouldReturnCreated() throws Exception {
        String requestJson = """
                {
                  "firstName": "Petr",
                  "lastName": "Petrov",
                  "birthYear": 1985
                }
                """;

        ClientResponse response = new ClientResponse();
        response.setClientId(2L);
        response.setFirstName("Petr");
        response.setLastName("Petrov");
        response.setFullName("Petr Petrov");

        when(clientService.createClient(any())).thenReturn(response);

        mockMvc.perform(post("/api/clients").contentType(MediaType.APPLICATION_JSON).content(requestJson)).andExpect(status().isCreated()).andExpect(jsonPath("$.clientId").value(2L)).andExpect(jsonPath("$.fullName").value("Petr Petrov"));

        verify(clientService, times(1)).createClient(any());
    }

    @Test
    @DisplayName("POST /api/clients: should return 400 with invalid request")
    @WithMockUser(roles = "USER")
    void createClient_shouldReturnBadRequestForInvalidInput() throws Exception {
        String invalidJson = """
                {
                  "firstName": "",
                  "lastName": "",
                  "birthYear": 1800
                }
                """;

        mockMvc.perform(post("/api/clients").contentType(MediaType.APPLICATION_JSON).content(invalidJson)).andExpect(status().isBadRequest());

        verify(clientService, never()).createClient(any());
    }

    @Test
    @DisplayName("PUT /api/clients/{id}: should return updated client")
    @WithMockUser(roles = "USER")
    void updateClient_shouldReturnUpdated() throws Exception {
        String requestJson = """
                {
                  "firstName": "Petr",
                  "lastName": "Petrov",
                  "birthYear": 1985
                }
                """;

        ClientResponse response = new ClientResponse();
        response.setClientId(1L);
        response.setFirstName("Petr");
        response.setLastName("Petrov");
        response.setFullName("Petr Petrov");

        when(clientService.updateClient(eq(1L), any())).thenReturn(response);

        mockMvc.perform(put("/api/clients/1").contentType(MediaType.APPLICATION_JSON).content(requestJson)).andExpect(status().isOk()).andExpect(jsonPath("$.clientId").value(1L)).andExpect(jsonPath("$.fullName").value("Petr Petrov"));

        verify(clientService, times(1)).updateClient(eq(1L), any());
    }

    @Test
    @DisplayName("PUT /api/clients/{id}: should return 404 when client not found")
    @WithMockUser(roles = "USER")
    void updateClient_shouldReturnNotFound() throws Exception {
        String requestJson = """
                {
                  "firstName": "Petr",
                  "lastName": "Petrov",
                  "birthYear": 1985
                }
                """;

        when(clientService.updateClient(eq(999L), any())).thenThrow(new ClientNotFoundException(999L));

        mockMvc.perform(put("/api/clients/999").contentType(MediaType.APPLICATION_JSON).content(requestJson)).andExpect(status().isNotFound());

        verify(clientService, times(1)).updateClient(eq(999L), any());
    }

    @Test
    @DisplayName("PUT /api/clients/{id}: should return 400 when invalid request")
    @WithMockUser(roles = "USER")
    void updateClient_shouldReturnBadRequest() throws Exception {
        String invalidJson = """
                {
                  "firstName": "",
                  "lastName": "",
                  "birthYear": 1800
                }
                """;

        mockMvc.perform(put("/api/clients/1").contentType(MediaType.APPLICATION_JSON).content(invalidJson)).andExpect(status().isBadRequest());

        verify(clientService, never()).updateClient(anyLong(), any());
    }

    @Test
    @DisplayName("DELETE /api/clients/{id}: should return 403 for USER")
    @WithMockUser(roles = "USER")
    void deleteClient_shouldReturnForbiddenForUser() throws Exception {
        mockMvc.perform(delete("/api/clients/1")).andExpect(status().isForbidden());

        verify(clientService, never()).deleteClient(anyLong());
    }

    @Test
    @DisplayName("DELETE /api/clients/{id}: should return 204 for ADMIN")
    @WithMockUser(roles = "ADMIN")
    void deleteClient_shouldReturnNoContentForAdmin() throws Exception {
        doNothing().when(clientService).deleteClient(1L);

        mockMvc.perform(delete("/api/clients/1")).andExpect(status().isNoContent());

        verify(clientService, times(1)).deleteClient(1L);
    }
}
