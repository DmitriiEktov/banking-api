package com.example.bankingapi.exceptions;

public class ClientNotFoundException extends RuntimeException {
    public ClientNotFoundException(Long id) {
        super("Client not found with id: " + id);
    }
}
