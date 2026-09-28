package com.example.bankingapi.repositories;

import com.example.bankingapi.models.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByClientIsNull();

    boolean existsByAccountNumber(String accountNumber);
}
