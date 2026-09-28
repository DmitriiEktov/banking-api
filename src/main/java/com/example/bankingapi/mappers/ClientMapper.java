package com.example.bankingapi.mappers;

import com.example.bankingapi.dto.account.AccountShortResponse;
import com.example.bankingapi.dto.client.ClientCreateRequest;
import com.example.bankingapi.dto.client.ClientResponse;
import com.example.bankingapi.dto.client.ClientUpdateRequest;
import com.example.bankingapi.models.Client;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ClientMapper {
    private final AccountMapper accountMapper;

    public ClientResponse toResponse(Client client) {
        ClientResponse response = new ClientResponse();

        response.setClientId(client.getClientId());
        response.setFirstName(client.getFirstName());
        response.setLastName(client.getLastName());
        response.setBirthYear(client.getBirthYear());
        response.setFullName(client.getFullName());

        List<AccountShortResponse> accounts = client.getAccounts().stream().map(accountMapper::toShortResponse).toList();
        response.setAccounts(accounts);

        return response;
    }

    public Client toEntity(ClientCreateRequest request) {
        Client client = new Client();
        client.setFirstName(request.getFirstName());
        client.setLastName(request.getLastName());
        client.setBirthYear(request.getBirthYear());
        return client;
    }

    public void updateEntity(Client client, ClientUpdateRequest request) {
        client.setFirstName(request.getFirstName());
        client.setLastName(request.getLastName());
        client.setBirthYear(request.getBirthYear());
    }
}
