package com.example.bankingapi.exceptions;

public class AccountNumberDuplicateException extends RuntimeException {
    public AccountNumberDuplicateException(String accountNumber) {
        super("Account with number: " + accountNumber + " already exists");
    }
}
