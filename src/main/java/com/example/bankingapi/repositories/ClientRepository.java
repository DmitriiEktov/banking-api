package com.example.bankingapi.repositories;

import com.example.bankingapi.models.Client;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<Client, Long> {
}
