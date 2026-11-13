package com.example.bankingapi.controllers;

import com.example.bankingapi.config.SecurityConfig;
import com.example.bankingapi.dto.account.AccountHolderResponse;
import com.example.bankingapi.dto.account.AccountResponse;
import com.example.bankingapi.enums.AccountType;
import com.example.bankingapi.exceptions.AccountNotFoundException;
import com.example.bankingapi.security.CustomUserDetailsService;
import com.example.bankingapi.security.JwtService;
import com.example.bankingapi.services.AccountService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
@Import(SecurityConfig.class)
public class AccountControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    private AccountService accountService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @Test
    @DisplayName("GET /api/accounts: should return list of accounts")
    @WithMockUser(roles = "USER")
    void getAllAccounts_shouldReturnList() throws Exception {
        AccountResponse response1 = new AccountResponse();
        response1.setAccountId(1L);
        response1.setAccountNumber("1a2a3a4a5a6a7a8a9a1a2a3");

        AccountResponse response2 = new AccountResponse();
        response2.setAccountId(2L);
        response2.setAccountNumber("1b2b3b4b5b6b7b8b9b1b2b3");

        when(accountService.getAllAccounts()).thenReturn(List.of(response1, response2));

        mockMvc.perform(get("/api/accounts")).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(jsonPath("$.length()").value(2)) // ← скобки ()
                .andExpect(jsonPath("$[0].accountId").value(1L)).andExpect(jsonPath("$[0].accountNumber").value("1a2a3a4a5a6a7a8a9a1a2a3")).andExpect(jsonPath("$[1].accountId").value(2L)).andExpect(jsonPath("$[1].accountNumber").value("1b2b3b4b5b6b7b8b9b1b2b3"));

        verify(accountService, times(1)).getAllAccounts();
    }

    @Test
    @DisplayName("GET /api/accounts/{id}: should return account")
    @WithMockUser(roles = "USER")
    void getAccountById_shouldReturnAccount() throws Exception {
        AccountResponse response = new AccountResponse();
        response.setAccountId(1L);
        response.setAccountNumber("1a2a3a4a5a6a7a8a9a1a2a3");
        response.setAccountType(AccountType.DEBIT);
        response.setBalance(new BigDecimal("100.00"));

        when(accountService.getAccountById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/accounts/1")).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(jsonPath("$.accountId").value(1L)).andExpect(jsonPath("$.accountNumber").value("1a2a3a4a5a6a7a8a9a1a2a3")).andExpect(jsonPath("$.accountType").value("DEBIT")).andExpect(jsonPath("$.balance").value(100.00));

        verify(accountService, times(1)).getAccountById(1L);
    }

    @Test
    @DisplayName("GET /api/accounts/{id}: should return 401 for anonymous account")
    void getAccountById_shouldReturnUnauthorizedForAnonymous() throws Exception {
        mockMvc.perform(get("/api/accounts/1")).andExpect(status().isUnauthorized());
        verify(accountService, never()).getAccountById(anyLong());
    }

    @Test
    @DisplayName("GET /api/accounts/{id}: should return 404 when account not found")
    @WithMockUser(roles = "USER")
    void getAccountById_shouldReturnNotFound() throws Exception {
        when(accountService.getAccountById(999L)).thenThrow(new AccountNotFoundException(999L));

        mockMvc.perform(get("/api/accounts/999")).andExpect(status().isNotFound()).andExpect(jsonPath("$.status").value(404)).andExpect(jsonPath("$.message").value(containsString("999")));

        verify(accountService, times(1)).getAccountById(999L);
    }

    @Test
    @DisplayName("POST /api/accounts: should return 201 with valid request")
    @WithMockUser(roles = "USER")
    void createAccount_shouldReturnCreated() throws Exception {
        String requestJson = """
                {
                "accountNumber": "1a2a3a4a5a6a7a8a9a1a2a3",
                "accountType": "DEBIT",
                "balance": 0
                }
                """;

        AccountResponse response = new AccountResponse();
        response.setAccountId(2L);
        response.setAccountNumber("1a2a3a4a5a6a7a8a9a1a2a3");
        response.setAccountType(AccountType.DEBIT);
        response.setBalance(BigDecimal.ZERO);

        when(accountService.createAccount(any())).thenReturn(response);

        mockMvc.perform(post("/api/accounts").contentType(MediaType.APPLICATION_JSON).content(requestJson)).andExpect(status().isCreated()).andExpect(jsonPath("$.accountId").value(2L)).andExpect(jsonPath("$.accountType").value("DEBIT")); // ← строка

        verify(accountService, times(1)).createAccount(any());
    }

    @Test
    @DisplayName("POST /api/accounts: should return 400 with invalid request")
    @WithMockUser(roles = "USER")
    void createAccount_shouldReturnBadRequestForInvalidInput() throws Exception {
        String invalidJson = """
                {
                "accountNumber": "",
                "accountType": null,
                "balance": -10
                }
                """;

        mockMvc.perform(post("/api/accounts").contentType(MediaType.APPLICATION_JSON).content(invalidJson)).andExpect(status().isBadRequest());

        verify(accountService, never()).createAccount(any());
    }

    @Test
    @DisplayName("PUT /api/accounts/{id}: should return updated account")
    @WithMockUser(roles = "USER")
    void updateAccount_shouldReturnUpdated() throws Exception {
        String requestJson = """
                {
                "accountType": "SAVINGS"
                }
                """;

        AccountResponse response = new AccountResponse();
        response.setAccountId(1L);
        response.setAccountNumber("1a2a3a4a5a6a7a8a9a1a2a3");
        response.setAccountType(AccountType.SAVINGS);
        response.setBalance(BigDecimal.ZERO);

        when(accountService.updateAccount(eq(1L), any())).thenReturn(response);

        mockMvc.perform(put("/api/accounts/1").contentType(MediaType.APPLICATION_JSON).content(requestJson)).andExpect(status().isOk()).andExpect(jsonPath("$.accountId").value(1L)).andExpect(jsonPath("$.accountType").value("SAVINGS")); // ← строка

        verify(accountService, times(1)).updateAccount(eq(1L), any());
    }

    @Test
    @DisplayName("PUT /api/accounts/{id}: should return 404 when account not found")
    @WithMockUser(roles = "USER")
    void updateAccount_shouldReturnNotFound() throws Exception {
        String requestJson = """
                {
                "accountType": "SAVINGS"
                }
                """;

        when(accountService.updateAccount(eq(999L), any())).thenThrow(new AccountNotFoundException(999L));

        mockMvc.perform(put("/api/accounts/999").contentType(MediaType.APPLICATION_JSON).content(requestJson)).andExpect(status().isNotFound());

        verify(accountService, times(1)).updateAccount(eq(999L), any());
    }

    @Test
    @DisplayName("PUT /api/accounts/{id}: should return 400 when invalid request")
    @WithMockUser(roles = "USER")
    void updateAccount_shouldReturnBadRequest() throws Exception {
        String invalidJson = """
                {
                "accountType": ""
                }
                """;

        mockMvc.perform(put("/api/accounts/1").contentType(MediaType.APPLICATION_JSON).content(invalidJson)).andExpect(status().isBadRequest());

        verify(accountService, never()).updateAccount(anyLong(), any());
    }

    @Test
    @DisplayName("DELETE /api/accounts/{id}: should return 403 for USER")
    @WithMockUser(roles = "USER")
    void deleteAccount_shouldReturnForbiddenForUser() throws Exception {
        mockMvc.perform(delete("/api/accounts/1")).andExpect(status().isForbidden());
        verify(accountService, never()).deleteAccount(anyLong());
    }

    @Test
    @DisplayName("DELETE /api/accounts/{id}: should return 204 for ADMIN")
    @WithMockUser(roles = "ADMIN")
    void deleteAccount_shouldReturnNoContentForAdmin() throws Exception {
        doNothing().when(accountService).deleteAccount(1L);

        mockMvc.perform(delete("/api/accounts/1")).andExpect(status().isNoContent());

        verify(accountService, times(1)).deleteAccount(1L);
    }

    @Test
    @DisplayName("POST /api/accounts/{id}/assign: should return assigned account")
    @WithMockUser(roles = "USER")
    void assignAccount_shouldReturnAssigned() throws Exception {
        String requestJson = """
                {
                    "clientId": 1
                }
                """;

        AccountHolderResponse holder = new AccountHolderResponse();
        holder.setClientId(1L);
        holder.setFullName("Ivan Ivanov");

        AccountResponse response = new AccountResponse();
        response.setAccountId(1L);
        response.setAccountNumber("1a2a3a4a5a6a7a8a9a1a2a3");
        response.setAccountType(AccountType.DEBIT);
        response.setBalance(BigDecimal.ZERO);
        response.setHolder(holder);

        when(accountService.assignAccount(eq(1L), any())).thenReturn(response);

        mockMvc.perform(post("/api/accounts/1/assign").contentType(MediaType.APPLICATION_JSON).content(requestJson)).andExpect(status().isOk()).andExpect(jsonPath("$.accountId").value(1L)).andExpect(jsonPath("$.holder.clientId").value(1L)) // ← проверка полей
                .andExpect(jsonPath("$.holder.fullName").value("Ivan Ivanov"));

        verify(accountService, times(1)).assignAccount(eq(1L), any());
    }

    @Test
    @DisplayName("POST /api/accounts/{id}/assign: should return 404 when account not found")
    @WithMockUser(roles = "USER")
    void assignAccount_shouldReturnNotFound() throws Exception {
        String requestJson = """
                {
                    "clientId": 1
                }
                """;

        when(accountService.assignAccount(eq(999L), any())).thenThrow(new AccountNotFoundException(999L));

        mockMvc.perform(post("/api/accounts/999/assign").contentType(MediaType.APPLICATION_JSON).content(requestJson)).andExpect(status().isNotFound());

        verify(accountService, times(1)).assignAccount(eq(999L), any());
    }

    @Test
    @DisplayName("POST /api/accounts/{id}/assign: should return 400 when invalid request")
    @WithMockUser(roles = "USER")
    void assignAccount_shouldReturnBadRequest() throws Exception {
        String invalidJson = """
                {
                    "clientId": null
                }
                """;

        mockMvc.perform(post("/api/accounts/1/assign").contentType(MediaType.APPLICATION_JSON).content(invalidJson)).andExpect(status().isBadRequest());

        verify(accountService, never()).assignAccount(anyLong(), any());
    }

    @Test
    @DisplayName("POST /api/accounts/{id}/release: should return released account")
    @WithMockUser(roles = "USER")
    void releaseAccount_shouldReturnReleased() throws Exception {
        AccountResponse response = new AccountResponse();
        response.setAccountId(1L);
        response.setAccountNumber("1a2a3a4a5a6a7a8a9a1a2a3");
        response.setAccountType(AccountType.DEBIT);
        response.setBalance(BigDecimal.ZERO);
        response.setHolder(null);

        when(accountService.releaseAccount(1L)).thenReturn(response);

        mockMvc.perform(post("/api/accounts/1/release")).andExpect(status().isOk()).andExpect(jsonPath("$.accountId").value(1L)).andExpect(jsonPath("$.holder").value(nullValue())); // ← nullValue()

        verify(accountService, times(1)).releaseAccount(1L);
    }

    @Test
    @DisplayName("POST /api/accounts/{id}/release: should return 404 when account not found")
    @WithMockUser(roles = "USER")
    void releaseAccount_shouldReturnNotFound() throws Exception {
        when(accountService.releaseAccount(999L)).thenThrow(new AccountNotFoundException(999L));

        mockMvc.perform(post("/api/accounts/999/release")).andExpect(status().isNotFound());

        verify(accountService, times(1)).releaseAccount(999L);
    }
}
