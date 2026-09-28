package com.example.bankingapi.services;

import com.example.bankingapi.dto.account.AccountAssignRequest;
import com.example.bankingapi.dto.account.AccountCreateRequest;
import com.example.bankingapi.dto.account.AccountResponse;
import com.example.bankingapi.dto.account.AccountUpdateRequest;
import com.example.bankingapi.exceptions.*;
import com.example.bankingapi.mappers.AccountMapper;
import com.example.bankingapi.models.Account;
import com.example.bankingapi.models.Client;
import com.example.bankingapi.repositories.AccountRepository;
import com.example.bankingapi.repositories.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final ClientRepository clientRepository;
    private final AccountMapper accountMapper;

    @Transactional(readOnly = true)
    public List<AccountResponse> getAllAccounts() {
        return accountRepository.findAll().stream().map(accountMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> getFreeAccounts() {
        return accountRepository.findByClientIsNull().stream().map(accountMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccountById(Long id) {
        Account account = accountRepository.findById(id).orElseThrow(() -> new AccountNotFoundException(id));
        return accountMapper.toResponse(account);
    }

    @Transactional
    public AccountResponse createAccount(AccountCreateRequest request) {
        if (accountRepository.existsByAccountNumber(request.getAccountNumber())) {
            throw new AccountNumberDuplicateException(request.getAccountNumber());
        }
        Account account = accountMapper.toEntity(request);
        Account saved = accountRepository.save(account);
        return accountMapper.toResponse(saved);
    }

    @Transactional
    public AccountResponse updateAccount(Long id, AccountUpdateRequest request) {
        Account account = accountRepository.findById(id).orElseThrow(() -> new AccountNotFoundException(id));

        accountMapper.updateEntity(account, request);
        return accountMapper.toResponse(account);
    }

    @Transactional
    public void deleteAccount(Long id) {
        if (!accountRepository.existsById(id)) {
            throw new AccountNotFoundException(id);
        }
        accountRepository.deleteById(id);
    }

    @Transactional
    public AccountResponse assignAccount(Long accountId, AccountAssignRequest request) {
        Account account = accountRepository.findById(accountId).orElseThrow(() -> new AccountNotFoundException(accountId));

        if (account.getClient() != null) {
            throw new AccountAlreadyAssignedException(accountId);
        }
        Client client = clientRepository.findById(request.getClientId()).orElseThrow(() -> new ClientNotFoundException(request.getClientId()));

        account.setClient(client);
        return accountMapper.toResponse(account);
    }

    @Transactional
    public AccountResponse releaseAccount(Long accountId) {
        Account account = accountRepository.findById(accountId).orElseThrow(() -> new AccountNotFoundException(accountId));

        if (account.getClient() == null) {
            throw new AccountAlreadyFreeException(accountId);
        }
        account.setClient(null);
        return accountMapper.toResponse(account);
    }
}
