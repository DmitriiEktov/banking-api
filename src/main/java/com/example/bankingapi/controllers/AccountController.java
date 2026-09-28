package com.example.bankingapi.controllers;

import com.example.bankingapi.dto.account.AccountAssignRequest;
import com.example.bankingapi.dto.account.AccountCreateRequest;
import com.example.bankingapi.dto.account.AccountResponse;
import com.example.bankingapi.dto.account.AccountUpdateRequest;
import com.example.bankingapi.services.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping
    public List<AccountResponse> getAllAccounts() {
        return accountService.getAllAccounts();
    }

    @GetMapping("/free")
    public List<AccountResponse> getFreeAccounts() {
        return accountService.getFreeAccounts();
    }

    @GetMapping("/{id}")
    public AccountResponse getAccountById(@PathVariable Long id) {
        return accountService.getAccountById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse createAccount(@Valid @RequestBody AccountCreateRequest request) {
        return accountService.createAccount(request);
    }

    @PutMapping("/{id}")
    public AccountResponse updateAccount(@PathVariable Long id, @Valid @RequestBody AccountUpdateRequest request) {
        return accountService.updateAccount(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAccount(@PathVariable Long id) {
        accountService.deleteAccount(id);
    }

    @PostMapping("/{id}/assign")
    public AccountResponse assignAccount(@PathVariable Long id, @Valid @RequestBody AccountAssignRequest request) {
        return accountService.assignAccount(id, request);
    }

    @PostMapping("/{id}/release")
    public AccountResponse releaseAccount(@PathVariable Long id) {
        return accountService.releaseAccount(id);
    }
}
