package com.example.bankingapi.services;

import com.example.bankingapi.dto.client.ClientCreateRequest;
import com.example.bankingapi.dto.client.ClientResponse;
import com.example.bankingapi.dto.client.ClientUpdateRequest;
import com.example.bankingapi.exceptions.ClientNotFoundException;
import com.example.bankingapi.mappers.ClientMapper;
import com.example.bankingapi.models.Client;
import com.example.bankingapi.repositories.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientService {
    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;

    @Transactional(readOnly = true)
    public List<ClientResponse> getAllClients() {
        return clientRepository.findAll().stream().map(clientMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ClientResponse getClientById(Long id) {
        Client client = clientRepository.findById(id).orElseThrow(() -> new ClientNotFoundException(id));
        return clientMapper.toResponse(client);
    }

    @Transactional
    public ClientResponse createClient(ClientCreateRequest request) {
        Client client = clientMapper.toEntity(request);
        Client saved = clientRepository.save(client);
        return clientMapper.toResponse(saved);
    }

    @Transactional
    public ClientResponse updateClient(Long id, ClientUpdateRequest request) {
        Client client = clientRepository.findById(id).orElseThrow(() -> new ClientNotFoundException(id));

        clientMapper.updateEntity(client, request);
        return clientMapper.toResponse(client);
    }

    @Transactional
    public void deleteClient(Long id) {
        if (!clientRepository.existsById(id)) {
            throw new ClientNotFoundException(id);
        }

        clientRepository.deleteById(id);
    }
}
