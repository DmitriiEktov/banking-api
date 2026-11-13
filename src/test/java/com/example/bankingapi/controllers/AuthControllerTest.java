package com.example.bankingapi.controllers;

import com.example.bankingapi.config.SecurityConfig;
import com.example.bankingapi.enums.Role;
import com.example.bankingapi.exceptions.UsernameAlreadyExistsException;
import com.example.bankingapi.models.User;
import com.example.bankingapi.repositories.UserRepository;
import com.example.bankingapi.security.CustomUserDetailsService;
import com.example.bankingapi.security.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private AuthenticationManager authenticationManager;

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

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername("newuser")
                .password("hashed_password")
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_USER")))
                .build();

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed_password");
        when(userDetailsService.loadUserByUsername("newuser")).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn("mocked_jwt_token");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("mocked_jwt_token"))
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.role").value("USER"));

        verify(userRepository, times(1)).save(any(User.class));
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

        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value(containsString("existinguser")));

        verify(userRepository, never()).save(any(User.class));
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

        verify(userRepository, never()).existsByUsername(any());
    }

    @Test
    @DisplayName("POSt /api/auth/login: should return 200 and token on success")
    void login_shouldReturnOkAndToken() throws Exception {
        String requestJson = """
                {
                  "username": "testuser",
                  "password": "password123"
                }
                """;

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername("testuser")
                .password("hashed_password")
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_USER")))
                .build();

        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn("mocked_jwt_token");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mocked_jwt_token"))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.role").value("ROLE_USER"));

        verify(authenticationManager, times(1)).authenticate(any());
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

        doThrow(new BadCredentialsException("Invalid username or password"))
                .when(authenticationManager).authenticate(any(Authentication.class));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Invalid username or password"));

        verify(userDetailsService, never()).loadUserByUsername(anyString());
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

        verify(authenticationManager, never()).authenticate(any());
    }
}
