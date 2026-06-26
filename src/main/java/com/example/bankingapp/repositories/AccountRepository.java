package com.example.bankingapp.repositories;

import com.example.bankingapp.models.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {
    List<Account> findByClientIsNull();
    List<Account> findByClient_ClientId(int clientId);
}
