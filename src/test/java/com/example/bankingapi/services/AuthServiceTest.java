package com.example.bankingapi.services;

import com.example.bankingapi.dto.auth.AuthRequest;
import com.example.bankingapi.dto.auth.AuthResponse;
import com.example.bankingapi.dto.auth.RegisterRequest;
import com.example.bankingapi.exceptions.UsernameAlreadyExistsException;
import com.example.bankingapi.models.User;
import com.example.bankingapi.repositories.UserRepository;
import com.example.bankingapi.security.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserDetailsService userDetailsService;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("register: should create user and return token")
    void register_shouldCreateUserAndReturnToken() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setPassword("password123");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed_password");

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername("newuser")
                .password("hashed_password")
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_USER")))
                .build();

        when(userDetailsService.loadUserByUsername("newuser")).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn("mocked_jwt_token");

        AuthResponse response = authService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.getUsername()).isEqualTo("newuser");
        assertThat(response.getRole()).isEqualTo("USER");
        assertThat(response.getToken()).isEqualTo("mocked_jwt_token");

        verify(userRepository, times(1)).save(any(User.class));
        verify(userDetailsService, times(1)).loadUserByUsername("newuser");
        verify(jwtService, times(1)).generateToken(userDetails);
    }

    @Test
    @DisplayName("register: should throw exception when username already exists")
    void register_shouldThrowExceptionWhenUsernameExists() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("existinguser");
        request.setPassword("password123");

        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(UsernameAlreadyExistsException.class)
                .hasMessageContaining("existinguser");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("login: should authenticate user and return token")
    void login_shouldAuthenticateAndReturnToken() {
        AuthRequest request = new AuthRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername("testuser")
                .password("hashed_password")
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_USER")))
                .build();

        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn("mocked_jwt_token");

        AuthResponse response = authService.login(request);

        assertThat(response).isNotNull();
        assertThat(response.getUsername()).isEqualTo("testuser");
        assertThat(response.getRole()).isEqualTo("USER");
        assertThat(response.getToken()).isEqualTo("mocked_jwt_token");

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userDetailsService, times(1)).loadUserByUsername("testuser");
        verify(jwtService, times(1)).generateToken(userDetails);
    }
}
