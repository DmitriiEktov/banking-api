package com.example.bankingapp.controllers;

import com.example.bankingapp.models.Account;
import com.example.bankingapp.models.Client;
import com.example.bankingapp.repositories.AccountRepository;
import com.example.bankingapp.repositories.ClientRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountRepository accountRepository;
    private final ClientRepository clientRepository;

    @Autowired
    public AccountController(AccountRepository accountRepository, ClientRepository clientRepository) {
        this.accountRepository = accountRepository;
        this.clientRepository = clientRepository;
    }

    @GetMapping
    public List<Account> getAllAccounts() { return accountRepository.findAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<Account> getAccountById(@PathVariable int id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + id));
        return ResponseEntity.ok(account);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Account createAccount(@Valid @RequestBody Account account) { return accountRepository.save(account); }

    @PutMapping("/{id}")
    public ResponseEntity<Account> updateAccount(@PathVariable int id, @Valid @RequestBody Account accountDetails) {
        Account existingAccount = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not foumd with id: " + id));

        existingAccount.setAccountNumber(accountDetails.getAccountNumber());
        existingAccount.setAccountType(accountDetails.getAccountType());
        existingAccount.setBalance(accountDetails.getBalance());

        Account updatedAccount = accountRepository.save(existingAccount);
        return ResponseEntity.ok(updatedAccount);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAccount(@PathVariable int id) { accountRepository.deleteById(id); }

    @PutMapping("/{accountId}/assign/{clientId}")
    public ResponseEntity<Account> assignAccountToClient(@PathVariable int accountId, @PathVariable int clientId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + accountId));
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found with id: " + clientId));

        account.setClient(client);
        return ResponseEntity.ok(accountRepository.save(account));
    }

    @PutMapping("/{id}/release")
    public ResponseEntity<Account> releaseAccount(@PathVariable int id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + id));

        account.setClient(null);
        return ResponseEntity.ok(accountRepository.save(account));
    }

    @GetMapping("/free")
    public List<Account> getFreeAccounts() {
        return accountRepository.findByClientIsNull();
    }

    @GetMapping("/client/{clientId}")
    public List<Account> getAccountByClient(@PathVariable int clientId) {
        return accountRepository.findByClient_ClientId(clientId);
    }
}
