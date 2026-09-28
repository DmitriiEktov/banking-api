package com.example.bankingapi.exceptions;

public class AccountAlreadyFreeException extends RuntimeException {
    public AccountAlreadyFreeException(Long id) {
        super("Account with id " + id + " is already free");
    }
}
