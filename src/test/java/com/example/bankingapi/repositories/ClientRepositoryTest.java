package com.example.bankingapi.repositories;

import com.example.bankingapi.models.Client;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
class ClientRepositoryTest {

    @Autowired
    private ClientRepository clientRepository;

    @Test
    @DisplayName("findByFirstNameAndLastName: should return client when found")
    void findByFirstNameAndLastName_shouldReturnClient() {
        clientRepository.save(new Client("Ivan", "Ivanov", 1990));

        List<Client> found = clientRepository.findByFirstNameAndLastName("Ivan", "Ivanov");

        assertThat(found).hasSize(1);
        assertThat(found.get(0).getFullName()).isEqualTo("Ivan Ivanov");
    }

    @Test
    @DisplayName("findByFirstNameAndLastName: should return multiple clients with same name but different years")
    void findByFirstNameAndLastName_shouldReturnMultipleClients() {
        clientRepository.save(new Client("Ivan", "Ivanov", 1990));
        clientRepository.save(new Client("Ivan", "Ivanov", 1995));

        List<Client> found = clientRepository.findByFirstNameAndLastName("Ivan", "Ivanov");

        assertThat(found).hasSize(2);
    }

    @Test
    @DisplayName("findByFirstNameAndLastName: should return empty list when not found")
    void findByFirstNameAndLastName_shouldReturnEmptyList() {
        List<Client> found = clientRepository.findByFirstNameAndLastName("Petr", "Petrov");
        assertThat(found).isEmpty();
    }
}

