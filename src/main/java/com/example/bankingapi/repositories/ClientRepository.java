package com.example.bankingapi.repositories;

import com.example.bankingapi.models.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {
    List<Client> findByFirstNameAndLastName(String firstName, String lastName);
}
