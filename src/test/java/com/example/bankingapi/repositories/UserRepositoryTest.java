package com.example.bankingapi.repositories;

import com.example.bankingapi.enums.Role;
import com.example.bankingapi.models.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("findByUsername: should return user when found")
    void findByUsername_shouldReturnUser() {
        userRepository.save(new User("admin", "hashed_pass", Role.ADMIN));

        Optional<User> found = userRepository.findByUsername("admin");

        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("admin");
        assertThat(found.get().getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    @DisplayName("findByUsername: should return empty when not found")
    void findByUsername_shouldReturnEmpty() {
        Optional<User> found = userRepository.findByUsername("ghost");
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("existsByUsername: should return true when exists")
    void existsByUsername_shouldReturnTrue() {
        userRepository.save(new User("testuser", "hashed_pass", Role.USER));
        assertThat(userRepository.existsByUsername("testuser")).isTrue();
    }

    @Test
    @DisplayName("existsByUsername: should return false when not exists")
    void existsByUsername_shouldReturnFalse() {
        assertThat(userRepository.existsByUsername("unknown")).isFalse();
    }
}
