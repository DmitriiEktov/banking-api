package com.example.bankingapi.exceptions;

public class AccountAlreadyAssignedException extends RuntimeException {
    public AccountAlreadyAssignedException(Long id) {
        super("Account with id " + id + " is already assigned");
    }
}
