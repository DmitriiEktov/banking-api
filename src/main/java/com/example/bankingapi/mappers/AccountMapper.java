package com.example.bankingapi.mappers;

import com.example.bankingapi.dto.account.*;
import com.example.bankingapi.models.Account;
import org.springframework.stereotype.Component;

@Component
public class AccountMapper {

    public AccountShortResponse toShortResponse(Account account) {
        AccountShortResponse response = new AccountShortResponse();

        response.setAccountId(account.getAccountId());
        response.setAccountNumber(account.getAccountNumber());
        response.setAccountType(account.getAccountType());
        response.setBalance(account.getBalance());
        return response;
    }

    public AccountResponse toResponse(Account account) {
        AccountResponse response = new AccountResponse();

        response.setAccountId(account.getAccountId());
        response.setAccountNumber(account.getAccountNumber());
        response.setAccountType(account.getAccountType());
        response.setBalance(account.getBalance());

        if (account.getClient() != null) {
            AccountHolderResponse holder = new AccountHolderResponse();
            holder.setClientId(account.getClient().getClientId());
            holder.setFullName(account.getClient().getFullName());
            response.setHolder(holder);
        }
        return response;
    }

    public Account toEntity(AccountCreateRequest request) {
        Account account = new Account();

        account.setAccountNumber(request.getAccountNumber());
        account.setAccountType(request.getAccountType());
        account.setBalance(request.getBalance());
        return account;
    }

    public void updateEntity(Account account, AccountUpdateRequest request) {
        account.setAccountType(request.getAccountType());
    }
}
