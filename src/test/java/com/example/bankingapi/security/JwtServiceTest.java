package com.example.bankingapi.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        String secret = "thisIsAVerySecretKeyForTestingPurposesOnly12345";
        long expiration = 1000 * 60 * 60; // 1 час
        jwtService = new JwtService(secret, expiration);

        userDetails = new User("testuser", "password", List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    @DisplayName("generateToken: should generate token and extract username correctly")
    void generateAndExtractToken_Success() {
        String token = jwtService.generateToken(userDetails);
        String extractedUsername = jwtService.extractUsername(token);

        assertThat(token).isNotBlank();
        assertThat(extractedUsername).isEqualTo("testuser");
    }

    @Test
    @DisplayName("isTokenValid: should return true for valid token")
    void isTokenValid_ReturnsTrueForValidToken() {
        String token = jwtService.generateToken(userDetails);

        boolean isValid = jwtService.isTokenValid(token, userDetails);

        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("isTokenValid: should return false for wrong user")
    void isTokenValid_ReturnsFalseForWrongUser() {
        String token = jwtService.generateToken(userDetails);

        UserDetails anotherUser = new User("otheruser", "password", List.of(new SimpleGrantedAuthority("ROLE_USER")));

        boolean isValid = jwtService.isTokenValid(token, anotherUser);

        assertThat(isValid).isFalse();
    }
}
