package com.example.bankingapi.controllers;

import com.example.bankingapi.config.SecurityConfig;
import com.example.bankingapi.dto.auth.AuthRequest;
import com.example.bankingapi.dto.auth.AuthResponse;
import com.example.bankingapi.dto.auth.RegisterRequest;
import com.example.bankingapi.exceptions.UsernameAlreadyExistsException;
import com.example.bankingapi.security.CustomUserDetailsService;
import com.example.bankingapi.security.JwtService;
import com.example.bankingapi.services.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @Test
    @DisplayName("POST /api/auth/register: should return 201 and token on success")
    void register_shouldReturnCreatedAndToken() throws Exception {
        String requestJson = """
                {
                  "username": "newuser",
                  "password": "password123"
                }
                """;

        AuthResponse mockResponse = new AuthResponse("mocked_jwt_token", "newuser", "USER");
        when(authService.register(any(RegisterRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("mocked_jwt_token"))
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.role").value("USER"));

        verify(authService, times(1)).register(any(RegisterRequest.class));
    }

    @Test
    @DisplayName("POST /api/auth/register: should return 409 when username already exists")
    void register_shouldReturnConflictWhenUsernameExists() throws Exception {
        String requestJson = """
                {
                  "username": "existinguser",
                  "password": "password123"
                }
                """;

        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new UsernameAlreadyExistsException("existinguser"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value(containsString("existinguser")));

        verify(authService, times(1)).register(any(RegisterRequest.class));
    }

    @Test
    @DisplayName("POST /api/auth/register: should return 400 when request is invalid")
    void register_shouldReturnBadRequestForInvalidInput() throws Exception {
        String invalidJson = """
                {
                  "username": "u",
                  "password": "123"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        verify(authService, never()).register(any());
    }

    @Test
    @DisplayName("POST /api/auth/login: should return 200 and token on success")
    void login_shouldReturnOkAndToken() throws Exception {
        String requestJson = """
                {
                  "username": "testuser",
                  "password": "password123"
                }
                """;

        AuthResponse mockResponse = new AuthResponse("mocked_jwt_token", "testuser", "USER");
        when(authService.login(any(AuthRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mocked_jwt_token"))
                .andExpect(jsonPath("$.username").value("testuser"));

        verify(authService, times(1)).login(any(AuthRequest.class));
    }

    @Test
    @DisplayName("POST /api/auth/login: should return 401 when credentials are invalid")
    void login_shouldReturnUnauthorizedForBadCredentials() throws Exception {
        String requestJson = """
                {
                  "username": "testuser",
                  "password": "wrongpassword"
                }
                """;

        when(authService.login(any(AuthRequest.class)))
                .thenThrow(new BadCredentialsException("Invalid username or password"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Invalid username or password"));

        verify(authService, times(1)).login(any(AuthRequest.class));
    }

    @Test
    @DisplayName("POST /api/auth/login: should return 400 when request is invalid")
    void login_shouldReturnBadRequestForEmptyFields() throws Exception {
        String invalidJson = """
                {
                  "username": "",
                  "password": ""
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        verify(authService, never()).login(any());
    }
}