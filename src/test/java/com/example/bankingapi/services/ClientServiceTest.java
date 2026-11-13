package com.example.bankingapi.services;

import com.example.bankingapi.dto.client.ClientCreateRequest;
import com.example.bankingapi.dto.client.ClientResponse;
import com.example.bankingapi.dto.client.ClientUpdateRequest;
import com.example.bankingapi.exceptions.ClientNotFoundException;
import com.example.bankingapi.mappers.ClientMapper;
import com.example.bankingapi.models.Client;
import com.example.bankingapi.repositories.ClientRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ClientMapper clientMapper;

    @InjectMocks
    private ClientService clientService;

    @Test
    @DisplayName("getAllClients: should return list of clients")
    void getAllClients_shouldReturnList() {
        Client client1 = new Client("Ivan", "Ivanov", 1900);
        client1.setClientId(1L);
        Client client2 = new Client("Petr", "Petrov", 1985);
        client2.setClientId(2L);

        ClientResponse response1 = new ClientResponse();
        response1.setClientId(1L);
        response1.setFullName("Ivan Ivanov");
        ClientResponse response2 = new ClientResponse();
        response2.setClientId(2L);
        response2.setFullName("Petr Petrov");

        when(clientRepository.findAll()).thenReturn(List.of(client1, client2));
        when(clientMapper.toResponse(client1)).thenReturn(response1);
        when(clientMapper.toResponse(client2)).thenReturn(response2);

        List<ClientResponse> actual = clientService.getAllClients();

        assertThat(actual).hasSize(2);
        assertThat(actual.get(0).getFullName()).isEqualTo("Ivan Ivanov");
        assertThat(actual.get(1).getFullName()).isEqualTo("Petr Petrov");

        verify(clientRepository, times(1)).findAll();
        verify(clientMapper, times(2)).toResponse(any(Client.class));
    }

    @Test
    @DisplayName("getAllClients: should return empty list when no clients")
    void getAllClients_shouldReturnEmptyList() {
        when(clientRepository.findAll()).thenReturn(List.of());

        List<ClientResponse> actual = clientService.getAllClients();

        assertThat(actual).isEmpty();

        verify(clientMapper, never()).toResponse(any(Client.class));
    }

    @Test
    @DisplayName("getClientById: should return client when found")
    void getClientById_shouldReturnClient() {
        Long id = 1L;
        Client client = new Client("Ivan", "Ivanov", 1990);
        client.setClientId(id);

        ClientResponse response = new ClientResponse();
        response.setClientId(id);
        response.setFullName("Ivan Ivanov");

        when(clientRepository.findById(id)).thenReturn(Optional.of(client));
        when(clientMapper.toResponse(client)).thenReturn(response);

        ClientResponse actual = clientService.getClientById(id);

        assertThat(actual).isNotNull();
        assertThat(actual.getClientId()).isEqualTo(id);
        assertThat(actual.getFullName()).isEqualTo("Ivan Ivanov");

        verify(clientRepository, times(1)).findById(id);
        verify(clientMapper, times(1)).toResponse(client);
    }

    @Test
    @DisplayName("getClientById: should throw ClientNotFoundException when client not found")
    void getClientById_NotFound_ThrowsException() {
        Long nonExistentId = 999L;

        when(clientRepository.findById(nonExistentId)).thenReturn(java.util.Optional.empty());

        org.junit.jupiter.api.Assertions.assertThrows(com.example.bankingapi.exceptions.ClientNotFoundException.class, () -> clientService.getClientById(nonExistentId));

        verifyNoInteractions(clientMapper);
    }

    @Test
    @DisplayName("createClient: should create client successfully")
    void createClient_Success() {
        ClientCreateRequest request = new ClientCreateRequest();
        request.setFirstName("Ivan");
        request.setLastName("Ivanov");
        request.setBirthYear(1990);

        Client clientToSave = new Client("Ivan", "Ivanov", 1990);
        Client savedClient = new Client("Ivan", "Ivanov", 1990);
        savedClient.setClientId(1L);

        ClientResponse expectedResponse = new ClientResponse();
        expectedResponse.setClientId(1L);
        expectedResponse.setFullName("Ivan Ivanov");

        when(clientMapper.toEntity(request)).thenReturn(clientToSave);
        when(clientRepository.save(any(Client.class))).thenReturn(savedClient);
        when(clientMapper.toResponse(savedClient)).thenReturn(expectedResponse);

        ClientResponse actual = clientService.createClient(request);

        assertThat(actual).isNotNull();
        assertThat(actual.getClientId()).isEqualTo(1L);
        assertThat(actual.getFullName()).isEqualTo("Ivan Ivanov");

        verify(clientMapper, times(1)).toEntity(request);
        verify(clientRepository, times(1)).save(clientToSave);
        verify(clientMapper, times(1)).toResponse(savedClient);
    }

    @Test
    @DisplayName("updateClient: should update client successfully")
    void updateClient_Success() {
        Long id = 1L;
        ClientUpdateRequest request = new ClientUpdateRequest();
        request.setFirstName("Petr");
        request.setLastName("Petrov");
        request.setBirthYear(1985);

        Client existing = new Client("Ivan", "Ivanov", 1990);
        existing.setClientId(id);

        ClientResponse response = new ClientResponse();
        response.setClientId(id);
        response.setFullName("Petr Petrov");

        when(clientRepository.findById(id)).thenReturn(Optional.of(existing));
        when(clientMapper.toResponse(existing)).thenReturn(response);

        ClientResponse actual = clientService.updateClient(id, request);

        assertThat(actual).isNotNull();
        assertThat(actual.getFullName()).isEqualTo("Petr Petrov");

        verify(clientRepository, times(1)).findById(id);
        verify(clientMapper, times(1)).updateEntity(existing, request);
        verify(clientMapper, times(1)).toResponse(existing);
        verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    @DisplayName("updateClient: should throw ClientNotFoundException when not found")
    void updateClient_NotFound_ThrowsException() {
        Long nonExistentId = 999L;
        ClientUpdateRequest request = new ClientUpdateRequest();
        request.setFirstName("Petr");
        request.setLastName("Petrov");
        request.setBirthYear(1985);

        when(clientRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clientService.updateClient(nonExistentId, request)).isInstanceOf(ClientNotFoundException.class);

        verify(clientMapper, never()).updateEntity(any(), any());
    }

    @Test
    @DisplayName("deleteClient: should delete client successfully")
    void deleteClient_Success() {
        Long id = 1L;

        when(clientRepository.existsById(id)).thenReturn(true);
        doNothing().when(clientRepository).deleteById(id);

        clientService.deleteClient(id);

        verify(clientRepository, times(1)).existsById(id);
        verify(clientRepository, times(1)).deleteById(id);
    }

    @Test
    @DisplayName("deleteClient: should throw ClientNotFoundException when not found")
    void deleteClient_NotFound_ThrowsException() {
        Long nonExistentId = 999L;

        when(clientRepository.existsById(nonExistentId)).thenReturn(false);

        assertThatThrownBy(() -> clientService.deleteClient(nonExistentId)).isInstanceOf(ClientNotFoundException.class);

        verify(clientRepository, never()).deleteById(anyLong());
    }
}